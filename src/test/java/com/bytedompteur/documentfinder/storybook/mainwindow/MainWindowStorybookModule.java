package com.bytedompteur.documentfinder.storybook.mainwindow;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import com.bytedompteur.documentfinder.ui.FileSystemAdapter;
import com.bytedompteur.documentfinder.ui.WindowManager;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.BaseMainWindowModule;
import dagger.Module;
import dagger.Provides;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

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
  static FulltextSearchService provideFulltextSearchService() {
    var mockedSearchService = Mockito.mock(FulltextSearchService.class);
    Mockito
      .when(mockedSearchService.getCurrentPathProcessed()).thenReturn(Flux.empty());
    return mockedSearchService;
  }

  @Provides
  @Singleton
  static ExecutorService provideExecutorService(@Named("numberOfThreads") int numberOfThreads) {
    return Executors.newFixedThreadPool(1);
  }

  @Provides
  @Singleton
  static WindowManager provideWindowManager() {
    return Mockito.mock(WindowManager.class);
  }
}
