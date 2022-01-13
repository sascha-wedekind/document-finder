package com.bytedompteur.documentfinder.directorywatcher.core;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

import java.nio.file.WatchEvent;

@RequiredArgsConstructor
@EqualsAndHashCode
public class WatchEventForTests<T> implements WatchEvent<T> {

  private final T context;
  private final Kind<T> kind;

  @Override
  public Kind<T> kind() {
    return kind;
  }

  @Override
  public int count() {
    return 0;
  }

  @Override
  public T context() {
    return context;
  }
}
