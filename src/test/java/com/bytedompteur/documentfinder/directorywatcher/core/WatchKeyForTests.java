package com.bytedompteur.documentfinder.directorywatcher.core;

import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.Watchable;
import java.util.List;
import lombok.Builder;
import lombok.Singular;

@Builder
public class WatchKeyForTests implements WatchKey {

  @Singular
  private List<WatchEvent<?>> events;

  @Override
  public boolean isValid() {
    return true;
  }

  @Override
  public List<WatchEvent<?>> pollEvents() {
    return events;
  }

  @Override
  public boolean reset() {
    return false;
  }

  @Override
  public void cancel() {

  }

  @Override
  public Watchable watchable() {
    throw new UnsupportedOperationException();
  }
}
