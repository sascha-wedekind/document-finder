package com.bytedompteur.documentfinder.filewalker.core;

import com.bytedompteur.documentfinder.PathUtil;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Sinks.Many;

import java.nio.file.Path;
import java.util.Set;

@RequiredArgsConstructor
public class WalkerFactory {

  private final WalkFileTreeAdapter walkFileTreeAdapter;
  private final PathUtil pathUtil;

  public WalkerRunnable create(Many<Path> sink, FileEndingMatcher matcher, Set<Path> pathsToWalk) {
    return new WalkerRunnable(
      sink,
      matcher,
      pathUtil.removeChildPaths(pathsToWalk),
      walkFileTreeAdapter
    );
  }
}
