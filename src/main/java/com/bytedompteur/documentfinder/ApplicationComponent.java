package com.bytedompteur.documentfinder;

import com.bytedompteur.documentfinder.filewalker.adapter.in.FileWalker;
import com.bytedompteur.documentfinder.filewalker.dagger.FileWalkerModule;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import com.bytedompteur.documentfinder.fulltextsearchengine.dagger.FulltextSearchEngineModule;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import com.bytedompteur.documentfinder.persistedqueue.dagger.PersistedQueueModule;
import dagger.BindsInstance;
import dagger.Component;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;

@Singleton
@Component(modules = {
  ApplicationModule.class,
  PersistedQueueModule.class,
  FulltextSearchEngineModule.class,
  FileWalkerModule.class
})
public interface ApplicationComponent {

  FulltextSearchService fulltextSearchService();

  FileWalker fileWalker();

  PersistedUniqueFileEventQueue queue();

  ExecutorService executorService();

  @Component.Builder
  interface Builder {
    @BindsInstance
    Builder numberOfThreads(@Named("numberOfThreads") int value);

    @BindsInstance
    Builder applicationHomeDirectory(@Named("applicationHomeDirectory") String value);

    ApplicationComponent build();
  }
}
