package com.bytedompteur.documentfinder.storybook.mainwindow;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.SearchResult;
import com.bytedompteur.documentfinder.ui.FileSystemAdapter;
import com.bytedompteur.documentfinder.ui.WindowManager;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.BaseMainWindowModule;
import dagger.Module;
import dagger.Provides;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;

import javax.inject.Named;
import javax.inject.Singleton;
import java.nio.file.Path;
import java.time.LocalDateTime;
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

    Mockito.when(mockedSearchService.findLastUpdated()).thenReturn(Flux.just(
      SearchResult.builder().path(Path.of("/a/b/file_1.txt")).fileCreated(LocalDateTime.now()).fileLastUpdated(LocalDateTime.now()).build(),
      SearchResult.builder().path(Path.of("/a/b/file_2.txt")).fileCreated(LocalDateTime.now()).fileLastUpdated(LocalDateTime.now()).build(),
      SearchResult.builder().path(Path.of("/a/b/file_3.txt")).fileCreated(LocalDateTime.now()).fileLastUpdated(LocalDateTime.now()).build(),
      SearchResult.builder().path(Path.of("/a/b/file_4.txt")).fileCreated(LocalDateTime.now()).fileLastUpdated(LocalDateTime.now()).build()
    ));

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
