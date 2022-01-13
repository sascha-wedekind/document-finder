package com.bytedompteur.documentfinder.storybook.mainwindow;

import com.bytedompteur.documentfinder.ui.FileSystemAdapter;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.BaseMainWindowModule;
import dagger.Module;
import dagger.Provides;
import org.mockito.Mockito;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Module(includes = BaseMainWindowModule.class)
public abstract class MainWindowStorybookModule {

  @Provides
  static FileSystemAdapter provideFileSystemAdapter() {
    return Mockito.mock(FileSystemAdapter.class);
  }

  @Provides
  @Singleton
  static ExecutorService provideExecutorService(@Named("numberOfThreads") int numberOfThreads) {
    return Executors.newFixedThreadPool(1);
  }
}
