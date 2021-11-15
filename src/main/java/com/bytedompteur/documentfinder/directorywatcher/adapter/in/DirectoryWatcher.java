package com.bytedompteur.documentfinder.directorywatcher.adapter.in;


import java.io.IOException;
import java.nio.file.Path;
import reactor.core.publisher.Flux;

public interface DirectoryWatcher {

  Flux<FileWatchEvent> fileEvents();

  void watchIncludingSubdirectories(Path value) throws IOException;

  void unwatchIncludingSubdirectories(Path value) throws IOException;

  void startWatching();

  void stopWatching();

  boolean isWatching();
}
