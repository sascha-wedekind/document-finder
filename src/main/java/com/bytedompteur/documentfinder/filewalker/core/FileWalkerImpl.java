package com.bytedompteur.documentfinder.filewalker.core;

import com.bytedompteur.documentfinder.PathUtil;
import com.bytedompteur.documentfinder.filewalker.adapter.in.FileWalker;
import com.bytedompteur.documentfinder.filewalker.adapter.in.FileWalkerAlreadyRunningException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.publisher.Sinks.Many;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@RequiredArgsConstructor
@Slf4j
public class FileWalkerImpl implements FileWalker {

  private final AtomicBoolean started = new AtomicBoolean(false);
  private final AtomicReference<WalkerRunnable> walkerReference = new AtomicReference<>();
  private final WalkerFactory walkerFactory;
  private final ExecutorService executorService;
  private final PathUtil pathUtil;

  @Override
  public boolean isRunning() {
    return started.get();
  }

  @Override
  public Flux<Path> findFilesWithEndings(
    Set<String> endings,
    Set<Path> pathsToWalk
  ) throws FileWalkerAlreadyRunningException {
    if (started.compareAndSet(false, true)) {
      Many<Path> sink = Sinks.many().unicast().onBackpressureBuffer();
      FileEndingMatcher matcher = new FileEndingMatcher(endings);
      WalkerRunnable walker = walkerFactory.create(
        sink,
        matcher,
        pathUtil.removeChildPaths(pathsToWalk)
      );
      walkerReference.set(walker);
      executorService.submit(walker);
      return sink
        .asFlux()
        .doOnComplete(() -> {
          started.compareAndSet(true, false);
          walkerReference.set(null);
        });
    } else {
      throw new FileWalkerAlreadyRunningException();
    }
  }

  @Override
  public void stop() {
    Optional.ofNullable(walkerReference.get()).ifPresent(WalkerRunnable::stop);
  }

}
