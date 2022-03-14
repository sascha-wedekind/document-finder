package com.bytedompteur.documentfinder.persistedqueue.dagger;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import com.bytedompteur.documentfinder.persistedqueue.core.FilesReadWriteAdapter;
import com.bytedompteur.documentfinder.persistedqueue.core.PersistedQueueItemCompactor;
import com.bytedompteur.documentfinder.persistedqueue.core.PersistedUniqueFileEventQueueImpl;
import com.bytedompteur.documentfinder.persistedqueue.core.QueueRepository;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.time.Clock;

@Module
public class PersistedQueueModule {

  @Provides
  @Singleton
  public PersistedUniqueFileEventQueue provideEventQueue(PersistedUniqueFileEventQueueImpl value) {
    return value;
  }

  @Provides
  @Singleton
  public QueueRepository provideQueueRepository(@Named("applicationHomeDirectory") String applicationHomeDirectory) {
    return new QueueRepository(
      new PersistedQueueItemCompactor(),
      applicationHomeDirectory,
      new FilesReadWriteAdapter(),
      Clock.systemDefaultZone()
    );
  }
}
