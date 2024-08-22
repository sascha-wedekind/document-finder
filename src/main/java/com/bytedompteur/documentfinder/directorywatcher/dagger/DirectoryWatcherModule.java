package com.bytedompteur.documentfinder.directorywatcher.dagger;

import com.bytedompteur.documentfinder.directorywatcher.adapter.in.DirectoryWatcher;
import com.bytedompteur.documentfinder.directorywatcher.adapter.out.FilesAdapter;
import com.bytedompteur.documentfinder.directorywatcher.core.DirectoryWatcherImpl;
import com.bytedompteur.documentfinder.directorywatcher.core.LazyDirectoryWatcherDelegate;
import com.bytedompteur.documentfinder.directorywatcher.core.WatchServicePollHandler;
import dagger.Module;
import dagger.Provides;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Singleton;
import java.util.concurrent.ExecutorService;

@Slf4j
@Module
public abstract class DirectoryWatcherModule {

  @Provides
  @Singleton
  static DirectoryWatcher provideDirectoryWatcher(ExecutorService executorService) {
    LazyDirectoryWatcherDelegate lazyDelegate = new LazyDirectoryWatcherDelegate();
    DirectoryWatcherImpl result = new DirectoryWatcherImpl(executorService, new WatchServicePollHandler(lazyDelegate), new FilesAdapter());
    lazyDelegate.setDelegate(result);
    return result;
  }

}
