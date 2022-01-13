package com.bytedompteur.documentfinder.filewalker.core;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Sinks.Many;

import java.nio.file.Path;
import java.util.Set;

@RequiredArgsConstructor
public class WalkerFactory {

  private final WalkFileTreeAdapter walkFileTreeAdapter;

  public WalkerRunnable create(Many<Path> sink, FileEndingMatcher matcher, Set<Path> pathsToWalk) {
    return new WalkerRunnable(
      sink,
      matcher,
      PathUtil.removeChildPaths(pathsToWalk),
      walkFileTreeAdapter
    );
  }
}
