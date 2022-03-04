package com.bytedompteur.documentfinder.ui.mainwindow.dagger;

import com.bytedompteur.documentfinder.fulltextsearchengine.dagger.FulltextSearchEngineModule;
import com.bytedompteur.documentfinder.ui.FileSystemAdapter;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Module(includes = {BaseMainWindowModule.class, FulltextSearchEngineModule.class})
public abstract class MainWindowModule {

  @Provides
  static FileSystemAdapter provideFileSystemAdapter() {
    return new FileSystemAdapter();
  }

  @Provides
  @Singleton
  static ExecutorService provideExecutorService(@Named("numberOfThreads") int numberOfThreads) {
    return Executors.newFixedThreadPool(Math.max(1, numberOfThreads));
  }
}
