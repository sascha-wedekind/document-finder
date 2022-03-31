package com.bytedompteur.documentfinder.commands.dagger;

import com.bytedompteur.documentfinder.directorywatcher.dagger.DirectoryWatcherModule;
import com.bytedompteur.documentfinder.filewalker.dagger.FileWalkerModule;
import com.bytedompteur.documentfinder.fulltextsearchengine.dagger.FulltextSearchEngineModule;
import com.bytedompteur.documentfinder.persistedqueue.dagger.PersistedQueueModule;
import dagger.Module;

@Module(includes = {
  FulltextSearchEngineModule.class,
  FileWalkerModule.class,
  DirectoryWatcherModule.class,
  PersistedQueueModule.class
})
public abstract class CommandsModule {
}
