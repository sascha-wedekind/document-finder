package com.bytedompteur.documentfinder.directorywatcher.core;

import com.bytedompteur.documentfinder.directorywatcher.adapter.in.DirectoryWatcher;
import com.bytedompteur.documentfinder.directorywatcher.adapter.in.FileWatchEvent;
import lombok.NonNull;
import lombok.Setter;
import org.apache.commons.lang3.Validate;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.Map;

/**
 * Helper class to allow to connect DirectoryWatcherImpl and WatchServicePollHandler during instance creation.
 */
@Setter
public class LazyDirectoryWatcherDelegate implements DirectoryWatcher {

  @NonNull
  private DirectoryWatcher delegate;

  @Override
  public Map<WatchKey, Path> getPathByWatchKey() {
    ensureDelegateIsGiven();
    return delegate.getPathByWatchKey();
  }

  @Override
  public Flux<FileWatchEvent> fileEvents() {
    ensureDelegateIsGiven();
    return delegate.fileEvents();
  }

  @Override
  public void watchIncludingSubdirectories(Path value) throws IOException {
    ensureDelegateIsGiven();
    delegate.watchIncludingSubdirectories(value);
  }

  @Override
  public void unwatchIncludingSubdirectories(Path value) throws IOException {
    ensureDelegateIsGiven();
    delegate.unwatchIncludingSubdirectories(value);
  }

  @Override
  public void startWatching() {
    ensureDelegateIsGiven();
    delegate.startWatching();
  }

  @Override
  public void stopWatching() {
    ensureDelegateIsGiven();
    delegate.stopWatching();
  }

  @Override
  public boolean isWatching() {
    ensureDelegateIsGiven();
    return delegate.isWatching();
  }

  private void ensureDelegateIsGiven() {
    Validate.notNull(delegate, "Delegate is required and must not be null!");
  }
}
