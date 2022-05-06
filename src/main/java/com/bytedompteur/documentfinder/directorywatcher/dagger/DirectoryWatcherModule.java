package com.bytedompteur.documentfinder.directorywatcher.dagger;

import com.bytedompteur.documentfinder.directorywatcher.adapter.in.DirectoryWatcher;
import com.bytedompteur.documentfinder.directorywatcher.core.DirectoryWatcherImpl;
import dagger.Module;
import dagger.Provides;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;

@Slf4j
@Module
public abstract class DirectoryWatcherModule {

  @Provides
  @Singleton
  static DirectoryWatcher provideDirectoryWatcher(ExecutorService executorService) {
    return new DirectoryWatcherImpl(executorService);
  }

}
