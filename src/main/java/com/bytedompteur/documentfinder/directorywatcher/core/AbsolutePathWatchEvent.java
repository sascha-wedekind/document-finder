package com.bytedompteur.documentfinder.directorywatcher.core;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

@RequiredArgsConstructor
@EqualsAndHashCode
public class AbsolutePathWatchEvent implements WatchEvent<Path> {

  private final Path absolutePath;
  private final WatchEvent<Path> event;

  @Override
  public Kind<Path> kind() {
    return event.kind();
  }

  @Override
  public int count() {
    return event.count();
  }

  @Override
  public Path context() {
    return absolutePath;
  }

  public boolean isDirectoryEvent() {
    return Files.isDirectory(absolutePath);
  }

  public boolean isFileEvent() {
    /*
     * Do not use Files.isRegularFile(absolutePath), because it returns false when kind is DELETE.
     */
    return !isDirectoryEvent();
  }
}
