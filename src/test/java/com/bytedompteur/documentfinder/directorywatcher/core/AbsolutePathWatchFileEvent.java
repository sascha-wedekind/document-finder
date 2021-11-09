package com.bytedompteur.documentfinder.directorywatcher.core;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public class AbsolutePathWatchFileEvent extends AbsolutePathWatchEvent {

  private AbsolutePathWatchFileEvent(Path absolutePath,WatchEvent<Path> event) {
    super(absolutePath, event);
  }

  @Override
  public boolean isDirectoryEvent() {
    return false;
  }

  @Override
  public boolean isFileEvent() {
    return true;
  }

  public static AbsolutePathWatchFileEvent create(AbsolutePathWatchEvent value) {
    return new AbsolutePathWatchFileEvent(value.context(), value);
  }
}
