package com.bytedompteur.documentfinder.directorywatcher.core;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public class AbsolutePathWatchDirectoryEvent extends AbsolutePathWatchEvent {

  private AbsolutePathWatchDirectoryEvent(Path absolutePath,WatchEvent<Path> event) {
    super(absolutePath, event);
  }

  @Override
  public boolean isDirectoryEvent() {
    return true;
  }

  @Override
  public boolean isFileEvent() {
    return false;
  }

  public static AbsolutePathWatchDirectoryEvent create(AbsolutePathWatchEvent value) {
    return new AbsolutePathWatchDirectoryEvent(value.context(), value);
  }
}
