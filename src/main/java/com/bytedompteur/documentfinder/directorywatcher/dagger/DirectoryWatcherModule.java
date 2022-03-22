package com.bytedompteur.documentfinder.directorywatcher.dagger;

import com.bytedompteur.documentfinder.directorywatcher.adapter.in.DirectoryWatcher;
import com.bytedompteur.documentfinder.directorywatcher.core.DirectoryWatcherImpl;
import dagger.Module;
import dagger.Provides;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
@Module
public class DirectoryWatcherModule {

  @Provides
  static DirectoryWatcher provideDirectoryWatcher() {
    DirectoryWatcherImpl result = null;
    try {
      result = new DirectoryWatcherImpl();
    } catch (IOException e) {
      log.error("Could no create directory watcher", e);
    }
    return result;
  }

}
