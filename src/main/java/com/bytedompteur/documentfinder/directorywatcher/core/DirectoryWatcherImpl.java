package com.bytedompteur.documentfinder.directorywatcher.core;

import com.bytedompteur.documentfinder.directorywatcher.adapter.in.DirectoryWatcher;
import com.bytedompteur.documentfinder.directorywatcher.adapter.in.FileWatchEvent;
import com.bytedompteur.documentfinder.directorywatcher.adapter.in.FileWatchEvent.Type;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.nonNull;

@Slf4j
public class DirectoryWatcherImpl implements DirectoryWatcher {
  private final Map<WatchKey, Path> pathByWatchKey = new ConcurrentHashMap<>();
  private final Map<Path, WatchKey> watchKeyByPath = new ConcurrentHashMap<>();
  private final AtomicBoolean started = new AtomicBoolean(false);
  private final AtomicBoolean shouldStop = new AtomicBoolean(false);
  private final WatchServicePollHandler pollHandler;
  private final ExecutorService executorService;
  private WatchService watchService;
  private Flux<AbsolutePathWatchEvent> eventEmitter;
  private Many<AbsolutePathWatchEvent> sink;

  public DirectoryWatcherImpl(ExecutorService executorService) {
    this.executorService = executorService;
    createSinkAndFlux();
    pollHandler = new WatchServicePollHandler(this);
  }

  protected DirectoryWatcherImpl(WatchServicePollHandler pollHandler, WatchService watchService, ExecutorService executorService) {
    this.executorService = executorService;
    createSinkAndFlux();
    this.pollHandler = pollHandler;
    this.watchService = watchService;
  }

  Map<WatchKey, Path> getPathByWatchKey() {
    return Collections.unmodifiableMap(pathByWatchKey);
  }

  @Override
  public Flux<FileWatchEvent> fileEvents() {
    return eventEmitter.map(this::mapToFileWatchEvent);
  }

  @Override
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

  @Override
  public void unwatchIncludingSubdirectories(Path value) throws IOException {
    if (nonNull(value) && isAlreadyRegistered(value)) {
      Files.walkFileTree(value, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
          deregisterAtWatchService(dir);
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }

  @Override
  public void startWatching() {
    if (started.compareAndSet(false, true)) {
      createWatchServiceIfRequired();
      executorService.submit(this::doWatch);
    }
  }

  private void createWatchServiceIfRequired() {
    try {
      if (watchService == null) {
        log.debug("Creating watch service");
        watchService = FileSystems.getDefault().newWatchService();
      }
    } catch (IOException e) {
      log.error("While creating watch service", e);
    }
  }

  @SuppressWarnings("java:S2142")
  private void doWatch() {
    while (!shouldStop.get()) {
      try {
        TimeUnit.SECONDS.sleep(2); // Reduce processor usage to be mor energy efficient.
      } catch (InterruptedException e) {
        // IGNORE
      }
      WatchKey key = watchService.poll();
      pollHandler.handlePoll(key).forEach(e -> sink.tryEmitNext(e));

    }

    try {
      watchService.close();
    } catch (IOException e) {
      log.error("While closing watch service", e);
    } finally {
      watchService = null;
    }
    shouldStop.compareAndSet(true, false);
    started.compareAndSet(true, false);
  }

  @Override
  public void stopWatching() {
    shouldStop.compareAndSet(false, true);
  }

  @Override
  public boolean isWatching() {
    return started.get();
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

  private FileWatchEvent mapToFileWatchEvent(AbsolutePathWatchEvent e) {
    Type type;
    if (StandardWatchEventKinds.ENTRY_CREATE.equals(e.kind())) {
      type = Type.CREATE;
    } else if (StandardWatchEventKinds.ENTRY_MODIFY.equals(e.kind())) {
      type = Type.UPDATE;
    } else {
      type = Type.DELETE;
    }
    return new FileWatchEvent(type, e.context());
  }
}
