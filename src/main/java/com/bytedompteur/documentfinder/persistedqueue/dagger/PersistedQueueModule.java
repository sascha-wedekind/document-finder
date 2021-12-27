package com.bytedompteur.documentfinder.persistedqueue.dagger;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import com.bytedompteur.documentfinder.persistedqueue.core.PersistedUniqueFileEventQueueImpl;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
public class PersistedQueueModule {

  @Provides
  @Singleton
  public PersistedUniqueFileEventQueue provideEventQueue(PersistedUniqueFileEventQueueImpl value) {
    return value;
  }

}
