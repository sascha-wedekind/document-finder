package com.bytedompteur.documentfinder.fulltextsearchengine.dagger;

import com.bytedompteur.documentfinder.DaggerProvideException;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out.FilesAdapter;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out.PersistedUniqueFileEventQueueAdapter;
import com.bytedompteur.documentfinder.fulltextsearchengine.core.*;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
// Remove old SearchHistoryManager import: import com.bytedompteur.documentfinder.searchhistory.SearchHistoryManager;
import com.bytedompteur.documentfinder.persistedqueue.dagger.PersistedQueueModule;
import com.bytedompteur.documentfinder.searchhistory.dagger.SearchHistoryModule; // Added import
import com.bytedompteur.documentfinder.searchhistory.adapter.in.SearchHistoryService; // Added import
import dagger.Module;
import dagger.Provides;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.index.IndexWriter;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

@Module(includes = {PersistedQueueModule.class, SearchHistoryModule.class}) // Added SearchHistoryModule
@Slf4j
public class FulltextSearchEngineModule {

  @Provides
  @Singleton
  public IndexManager provideIndexManager(@Named("applicationHomeDirectory") String value) {
    var indexManager = new IndexManager(value);
    indexManager.init();
    return indexManager;
  }

  @Provides
  @Singleton
  public IndexWriter provideIndexWriter(IndexManager value) {
    try {
      return value.buildIndexWriter();
    } catch (IOException e) {
      throw new DaggerProvideException("Unable to create IndexWriter", e);
    }
  }

  @Provides
  @Singleton
  public IndexRepository provideIndexRepository(IndexWriter writer, IndexManager indexManager) {
    return new IndexRepository(writer, new IndexSearcherFactory(writer, indexManager));
  }

  @Provides
  @Singleton
  public PersistedUniqueFileEventQueueAdapter providePersistedUniqueFileEventQueueAdapter(
    PersistedUniqueFileEventQueue queue
  ) {
    return new PersistedUniqueFileEventQueueAdapter(queue);
  }

  @Provides
  @Singleton
  public FileEventHandler provideFileEventHandler(
    IndexRepository repository,
    PersistedUniqueFileEventQueueAdapter adapter,
    ExecutorService executorService
  ) {
    return new FileEventHandler(executorService, repository, adapter, new FilesAdapter());
  }

  @Provides
  @Singleton
  public FulltextSearchService provideFulltextSearchService(
      FileEventHandler handler,
      IndexRepository repository,
      SearchHistoryService searchHistoryService // Changed parameter type
  ) {
    return new FulltextSearchServiceImpl(handler, repository, searchHistoryService); // Pass new type
  }

  // Removed provideSearchHistoryManager() method as it's now in SearchHistoryModule
}
