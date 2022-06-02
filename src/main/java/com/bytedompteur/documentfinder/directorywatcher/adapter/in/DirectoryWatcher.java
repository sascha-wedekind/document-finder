package com.bytedompteur.documentfinder.directorywatcher.adapter.in;


import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.Map;

public interface DirectoryWatcher {

  Map<WatchKey, Path> getPathByWatchKey();

  Flux<FileWatchEvent> fileEvents();

  void watchIncludingSubdirectories(Path value) throws IOException;

  void unwatchIncludingSubdirectories(Path value) throws IOException;

  void startWatching();

  void stopWatching();

  boolean isWatching();
}
