package com.bytedompteur.documentfinder.filewalker.adapter.in;

import java.nio.file.Path;
import java.util.Set;
import reactor.core.publisher.Flux;

public interface FileWalker {

  boolean isRunning();

  Flux<Path> findFilesWithEndings(Set<String> endings, Set<Path> pathsToWalk) throws FileWalkerAlreadyRunningException;

  void stop();
}
