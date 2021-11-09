package com.bytedompteur.documentfinder.directorywatcher.core;

import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;

public class WatchEventBuilder {

  private Path path;
  private Kind<Path> kind;

  private WatchEventBuilder() {}

  public static WatchEventBuilder eventBuilder() {
    return new WatchEventBuilder();
  }

  public WatchEventBuilder path(Path value) {
    path = value;
    return this;
  }

  public WatchEventBuilder kind(WatchEvent.Kind<Path> value) {
    kind = value;
    return this;
  }

  public AbsolutePathWatchEvent buildEvent() {
    return new AbsolutePathWatchEvent(path, new WatchEventForTests<>(path, kind));
  }

  public AbsolutePathWatchFileEvent buildFileEvent() {
    return AbsolutePathWatchFileEvent.create(buildEvent());
  }

  public AbsolutePathWatchDirectoryEvent buildDirectoryEvent() {
    return AbsolutePathWatchDirectoryEvent.create(buildEvent());
  }
}
