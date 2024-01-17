package com.bytedompteur.documentfinder.persistedqueue.core;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.FileEvent;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import com.bytedompteur.documentfinder.persistedqueue.adapter.out.SettingsServiceAdapter;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class FilteringFileEventQueueDelegate implements PersistedUniqueFileEventQueue {
  private final PersistedUniqueFileEventQueue delegate;

  private final FileEndingMatcher fileEndingMatcher;


  public FilteringFileEventQueueDelegate(PersistedUniqueFileEventQueue delegate, SettingsServiceAdapter settingsService) {
    this.delegate = delegate;

    var settings = settingsService.getSettings();
    var fileTypes = settings.getFileTypes().stream().collect(Collectors.toUnmodifiableSet());
    fileEndingMatcher = new FileEndingMatcher(fileTypes);
  }

  @Override
  public void pushOrOverwrite(FileEvent value) {
    if (isEventContainingAllowedFileEnding(value)) {
      delegate.pushOrOverwrite(value);
    }
  }

  @Override
  public void pushOrOverwrite(List<FileEvent> value) {
    delegate.pushOrOverwrite(Optional
      .ofNullable(value)
      .orElseGet(List::of)
      .stream()
      .filter(this::isEventContainingAllowedFileEnding)
      .toList()
    );
  }

  @Override
  public Optional<FileEvent> pop() {
    return delegate.pop();
  }

  @Override
  public boolean isEmpty() throws InterruptedException {
    return delegate.isEmpty();
  }

  @Override
  public int size() throws InterruptedException {
    return delegate.size();
  }

  @Override
  public void clear() {
    delegate.clear();
  }

  @Override
  public Long getNumberOfFilesAdded() {
    return delegate.getNumberOfFilesAdded();
  }

  @Override
  public Long getNumberOfFilesRemoved() {
    return delegate.getNumberOfFilesRemoved();
  }

  private boolean isEventContainingAllowedFileEnding(FileEvent value) {
    return Objects.nonNull(value) && fileEndingMatcher.matches(value.getPath());
  }
}
