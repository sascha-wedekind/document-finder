package com.bytedompteur.documentfinder.filewalker.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Sinks.Many;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.Objects.requireNonNullElse;

@RequiredArgsConstructor
@Slf4j
class WalkerRunnable extends SimpleFileVisitor<Path> implements Runnable {

  private final Many<Path> sink;
  private final FileEndingMatcher fileEndingMatcher;
  private final Set<Path> pathsToWalk;
  private final AtomicBoolean shallStop = new AtomicBoolean(false);
  private final WalkFileTreeAdapter walkFileTreeAdapter;
  private Long numberOfMatchingFiles = 0L;
  private Long numberOfNonMatchingFiles = 0L;

  @Override
  public void run() {
    numberOfMatchingFiles = 0L;
    numberOfNonMatchingFiles = 0L;
    requireNonNullElse(pathsToWalk, Set.<Path>of())
      .forEach(it -> {
      try {
        if (!shallStop.get()) {
          walkFileTreeAdapter.walkFileTree(it, this);
        }
      } catch (IOException e) {
        log.error("While walking directory '{}'", it, e);
      }
    });
    log.info("Found {} files matching and {} files not matching", numberOfMatchingFiles, numberOfNonMatchingFiles);
    sink.tryEmitComplete();
  }

  @Override
  public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
    log.debug("Searching in '{}'", dir);
    return shallStop.get() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
    if (fileEndingMatcher.matches(file)) {
      log.debug("File '{}' matches filter options", file);
      numberOfMatchingFiles++;
      sink.tryEmitNext(file);
    } else {
      numberOfNonMatchingFiles++;
    }
    return shallStop.get() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
  }

  void stop() {
    shallStop.compareAndSet(false, true);
  }

  boolean isStopped() {
    return shallStop.get();
  }
}
