package com.bytedompteur.documentfinder.fulltextsearchengine.dagger;

import com.bytedompteur.documentfinder.DaggerProvideException;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out.PersistedUniqueFileEventQueueAdapter;
import com.bytedompteur.documentfinder.fulltextsearchengine.core.FileEventHandler;
import com.bytedompteur.documentfinder.fulltextsearchengine.core.FulltextSearchServiceImpl;
import com.bytedompteur.documentfinder.fulltextsearchengine.core.IndexManager;
import com.bytedompteur.documentfinder.fulltextsearchengine.core.IndexRepository;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import com.bytedompteur.documentfinder.persistedqueue.dagger.PersistedQueueModule;
import dagger.Module;
import dagger.Provides;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.index.IndexWriter;

import javax.inject.Singleton;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

@Module(includes = PersistedQueueModule.class)
@Slf4j
public class FulltextSearchEngineModule {

  @Provides
  @Singleton
  public IndexManager provideIndexManager() {
    var indexManager = IndexManager.getInstance();
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
  public IndexRepository provideIndexRepository(IndexWriter value) {
    return new IndexRepository(value);
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
    return new FileEventHandler(executorService, repository, adapter);
  }

  @Provides
  @Singleton
  public FulltextSearchService provideFulltextSearchService(FileEventHandler handler, IndexRepository repository) {
    return new FulltextSearchServiceImpl(handler, repository);
  }
}
