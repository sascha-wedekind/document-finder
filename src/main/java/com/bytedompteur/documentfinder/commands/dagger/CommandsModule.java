package com.bytedompteur.documentfinder.commands.dagger;

import com.bytedompteur.documentfinder.directorywatcher.dagger.DirectoryWatcherModule;
import com.bytedompteur.documentfinder.filewalker.dagger.FileWalkerModule;
import com.bytedompteur.documentfinder.fulltextsearchengine.dagger.FulltextSearchEngineModule;
import com.bytedompteur.documentfinder.interprocesscommunication.dagger.IPCModule;
import com.bytedompteur.documentfinder.persistedqueue.dagger.PersistedQueueModule;
import com.bytedompteur.documentfinder.ui.dagger.UIModule;
import dagger.Module;

@Module(includes = {
  FulltextSearchEngineModule.class,
  FileWalkerModule.class,
  DirectoryWatcherModule.class,
  PersistedQueueModule.class,
  IPCModule.class,
  UIModule.class
})
public abstract class CommandsModule {
}
