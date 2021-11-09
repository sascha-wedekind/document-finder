package com.bytedompteur.documentfinder.directorywatcher.core;

import java.nio.file.WatchEvent;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

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
