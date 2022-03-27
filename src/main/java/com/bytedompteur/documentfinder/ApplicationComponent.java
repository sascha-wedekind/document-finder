package com.bytedompteur.documentfinder;

import com.bytedompteur.documentfinder.commands.*;
import com.bytedompteur.documentfinder.commands.dagger.CommandsModule;
import com.bytedompteur.documentfinder.filewalker.adapter.in.FileWalker;
import com.bytedompteur.documentfinder.filewalker.dagger.FileWalkerModule;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import com.bytedompteur.documentfinder.fulltextsearchengine.dagger.FulltextSearchEngineModule;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import com.bytedompteur.documentfinder.persistedqueue.core.QueueRepository;
import com.bytedompteur.documentfinder.persistedqueue.dagger.PersistedQueueModule;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import com.bytedompteur.documentfinder.settings.dagger.SettingsModule;
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
  FileWalkerModule.class,
  SettingsModule.class,
  CommandsModule.class
})
public interface ApplicationComponent {

  FulltextSearchService fulltextSearchService();

  FileWalker fileWalker();

  PersistedUniqueFileEventQueue queue();

  ExecutorService executorService();

  SettingsService settingsService();

  StartAllCommand startAllCommand();

  ClearAllCommand clearAllCommand();

  QueueRepository queueRepository();

  WaitUntilFulltextSearchServiceProcessedAllEventsCommand waitUntilFulltextSearchServiceProcessedAllEventsCommand();

  WaitUntilPersistedQueueIsEmptyCommand waitUntilPersistedQueueIsEmptyCommand();

  StopAllGracefulCommand stopAllGracefulCommand();

  @Component.Builder
  interface Builder {
    @BindsInstance
    Builder numberOfThreads(@Named("numberOfThreads") int value);

    @BindsInstance
    Builder applicationHomeDirectory(@Named("applicationHomeDirectory") String value);

    ApplicationComponent build();
  }
}
