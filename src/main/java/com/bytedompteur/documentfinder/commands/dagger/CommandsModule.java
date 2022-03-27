package com.bytedompteur.documentfinder.commands.dagger;

import com.bytedompteur.documentfinder.commands.*;
import com.bytedompteur.documentfinder.directorywatcher.adapter.in.DirectoryWatcher;
import com.bytedompteur.documentfinder.directorywatcher.dagger.DirectoryWatcherModule;
import com.bytedompteur.documentfinder.filewalker.adapter.in.FileWalker;
import com.bytedompteur.documentfinder.filewalker.dagger.FileWalkerModule;
import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import com.bytedompteur.documentfinder.fulltextsearchengine.dagger.FulltextSearchEngineModule;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import com.bytedompteur.documentfinder.persistedqueue.dagger.PersistedQueueModule;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;

@Module(includes = {
  FulltextSearchEngineModule.class,
  FileWalkerModule.class,
  DirectoryWatcherModule.class,
  PersistedQueueModule.class
})
public abstract class CommandsModule {

  @Provides
  @Singleton
  static StopFulltextSearchServiceCommand provideStopFulltextSearchServiceCommand(FulltextSearchService service) {
    return new StopFulltextSearchServiceCommand(service);
  }

  @Provides
  @Singleton
  static StopFileWalkerCommand provideStopFileWalkerCommand(FileWalker fileWalker) {
    return new StopFileWalkerCommand(fileWalker);
  }

  @Provides
  @Singleton
  static StopDirectoryWatcherCommand provideStopDirectoryWatcherCommand(DirectoryWatcher watcher) {
    return new StopDirectoryWatcherCommand(watcher);
  }

  @Provides
  @Singleton
  static StopAllCommand provideStopAllCommand(
    ExecutorService executorService,
    StopDirectoryWatcherCommand stopDirectoryWatcherCommand,
    StopFileWalkerCommand stopFileWalkerCommand,
    StopFulltextSearchServiceCommand stopFulltextSearchServiceCommand
  ) {
    return new StopAllCommand(
      executorService,
      stopDirectoryWatcherCommand,
      stopFileWalkerCommand,
      stopFulltextSearchServiceCommand
    );
  }

  @Provides
  @Singleton
  static ClearPersistedQueueCommand provideClearPersistedQueueCommand(PersistedUniqueFileEventQueue queue) {
    return new ClearPersistedQueueCommand(queue);
  }

  @Provides
  @Singleton
  static ClearFulltextSearchServiceIndexCommand provideClearFulltextSearchServiceIndexCommand(FulltextSearchService servide) {
    return new ClearFulltextSearchServiceIndexCommand(servide);
  }

  @Provides
  @Singleton
  static ClearAllCommand provideClearAllCommand(
    ExecutorService executorService,
    ClearFulltextSearchServiceIndexCommand clearFulltextSearchServiceIndexCommand,
    ClearPersistedQueueCommand clearPersistedQueueCommand
  ) {
    return new ClearAllCommand(
      executorService,
      clearFulltextSearchServiceIndexCommand,
      clearPersistedQueueCommand
    );
  }

  @Provides
  @Singleton
  static StartFulltextSearchServiceCommand provideStartFulltextSearchServiceCommand(FulltextSearchService service) {
    return new StartFulltextSearchServiceCommand(service);
  }

  @Provides
  @Singleton
  static StartFileWalkerCommand provideStartFileWalkerCommand(
    FileWalker fileWalker,
    PersistedUniqueFileEventQueue queue,
    SettingsService settingsService
  ) {
    return new StartFileWalkerCommand(
      fileWalker,
      queue,
      settingsService
    );
  }

  @Provides
  @Singleton
  static StartDirectoryWatcherCommand provideStartDirectoryWatcherCommand(DirectoryWatcher directoryWatcher) {
    return new StartDirectoryWatcherCommand(directoryWatcher);
  }

  @Provides
  @Singleton
  static StartAllCommand provideStartAllCommand(
    StartFulltextSearchServiceCommand startFulltextSearchServiceCommand,
    StartDirectoryWatcherCommand startDirectoryWatcherCommand,
    StartFileWalkerCommand startFileWalkerCommand
  ) {
    return new StartAllCommand(
      startFulltextSearchServiceCommand,
      startDirectoryWatcherCommand,
      startFileWalkerCommand
    );
  }

  @Provides
  @Singleton
  static WaitUntilPersistedQueueIsEmptyCommand provideWaitUntilPersistedQueueIsEmptyCommand(
    PersistedUniqueFileEventQueue queue
  ) {
    return new WaitUntilPersistedQueueIsEmptyCommand(queue);
  }

  @Provides
  @Singleton
  static WaitUntilFulltextSearchServiceProcessedAllEventsCommand provideWaitUntilFulltextSearchServiceProcessedAllEventsCommand(
    FulltextSearchService searchService
  ) {
    return new WaitUntilFulltextSearchServiceProcessedAllEventsCommand(searchService);
  }

  @Provides
  @Singleton
  static StopAllGracefulCommand provideStopAllGracefulCommand(
    ExecutorService executorService,
    StopDirectoryWatcherCommand stopDirectoryWatcherCommand,
    StopFileWalkerCommand stopFileWalkerCommand,
    StopFulltextSearchServiceCommand stopFulltextSearchServiceCommand,
    WaitUntilPersistedQueueIsEmptyCommand waitUntilPersistedQueueIsEmptyCommand,
    WaitUntilFulltextSearchServiceProcessedAllEventsCommand waitUntilFulltextSearchServiceProcessedAllEventsCommand
  ) {
    return new StopAllGracefulCommand(
      executorService,
      stopDirectoryWatcherCommand,
      stopFileWalkerCommand,
      stopFulltextSearchServiceCommand,
      waitUntilPersistedQueueIsEmptyCommand,
      waitUntilFulltextSearchServiceProcessedAllEventsCommand
    );
  }
}
