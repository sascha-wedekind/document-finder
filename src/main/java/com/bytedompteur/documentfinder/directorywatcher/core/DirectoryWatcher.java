package com.bytedompteur.documentfinder.directorywatcher.core;

import static java.util.Objects.nonNull;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

@Slf4j
public class DirectoryWatcher {
  private final Map<WatchKey, Path> pathByWatchKey = new ConcurrentHashMap<>();
  private final Map<Path, WatchKey> watchKeyByPath = new ConcurrentHashMap<>();
  private final AtomicBoolean started = new AtomicBoolean(false);
  private final AtomicBoolean shouldStop = new AtomicBoolean(false);
  private final WatchServicePollHandler pollHandler;
  private final WatchService watchService;
  private Flux<AbsolutePathWatchEvent> eventEmitter;
  private Many<AbsolutePathWatchEvent> sink;

  public DirectoryWatcher() throws IOException {
    createSinkAndFlux();
    watchService = FileSystems.getDefault().newWatchService();
    pollHandler = new WatchServicePollHandler(this);
  }

  protected DirectoryWatcher(WatchServicePollHandler pollHandler, WatchService watchService) {
    createSinkAndFlux();
    this.pollHandler = pollHandler;
    this.watchService = watchService;
  }

  Map<WatchKey, Path> getPathByWatchKey() {
    return Collections.unmodifiableMap(pathByWatchKey);
  }

  public Flux<AbsolutePathWatchEvent> fileEvents() {
    return eventEmitter;
  }

  public void watchIncludingSubdirectories(Path value) throws IOException {
    if (nonNull(value) && !isAlreadyRegistered(value)) {
      Files.walkFileTree(value, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          registerAtWatchService(dir);
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }

  public void unwatchIncludingSubdirectories(Path value) throws IOException {
    if (nonNull(value) && isAlreadyRegistered(value)) {
      Files.walkFileTree(value, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
          deregisterAtWatchService(dir);
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }

  protected void startWatching() {
    if (started.compareAndSet(false, true)) {
      new Thread(this::doWatch).start();
    }
  }

  private void doWatch() {
    while (!shouldStop.get()) {
      WatchKey key = watchService.poll();
      pollHandler.handlePoll(key).forEach(e -> sink.tryEmitNext(e));

    }

    try {
      watchService.close();
    } catch (IOException e) {
      log.error("While closing watch service", e);
    }
    shouldStop.compareAndSet(true, false);
    started.compareAndSet(true, false);
  }

  protected void stopWatching() {
    shouldStop.compareAndSet(false, true);
  }

  private void deregisterAtWatchService(Path path) {
    if (isAlreadyRegistered(path)) {
      WatchKey watchKey = watchKeyByPath.get(path);
      watchKeyByPath.remove(path);
      pathByWatchKey.remove(watchKey);
      watchKey.cancel();
      log.debug("Unregistered '{}'", path);
    } else {
      log.debug("Already unregistered '{}'", path);
    }
  }

  private void registerAtWatchService(Path path) throws IOException {
    if (!isAlreadyRegistered(path)) {
      WatchKey watchKey = path.register(
        watchService,
        StandardWatchEventKinds.ENTRY_CREATE,
        StandardWatchEventKinds.ENTRY_DELETE,
        StandardWatchEventKinds.ENTRY_MODIFY
      );
      watchKeyByPath.put(path, watchKey);
      pathByWatchKey.put(watchKey, path);
      log.debug("Registered '{}'", path);
    } else {
      log.debug("Already registered '{}'", path);
    }
  }

  private boolean isAlreadyRegistered(Path path) {
    return watchKeyByPath.containsKey(path);
  }


  private void createSinkAndFlux() {
    // Non serialized and thread safe.
    sink = Sinks.unsafe().many().multicast().onBackpressureBuffer();
    eventEmitter = sink.asFlux();
  }
}
