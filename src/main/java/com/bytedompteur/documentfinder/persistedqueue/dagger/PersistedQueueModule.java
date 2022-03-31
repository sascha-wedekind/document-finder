package com.bytedompteur.documentfinder.persistedqueue.dagger;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.QueueRepository;
import com.bytedompteur.documentfinder.persistedqueue.adapter.out.FilesReadWriteAdapter;
import com.bytedompteur.documentfinder.persistedqueue.core.PersistedQueueItemCompactor;
import com.bytedompteur.documentfinder.persistedqueue.core.PersistedUniqueFileEventQueueImpl;
import com.bytedompteur.documentfinder.persistedqueue.core.QueueRepositoryImpl;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.time.Clock;

@Module
public abstract class PersistedQueueModule {

  @Provides
  @Singleton
  static PersistedUniqueFileEventQueue provideEventQueue(PersistedUniqueFileEventQueueImpl value) {
    return value;
  }

  @Provides
  @Singleton
  static QueueRepositoryImpl provideQueueRepository(@Named("applicationHomeDirectory") String applicationHomeDirectory) {
    return new QueueRepositoryImpl(
      new PersistedQueueItemCompactor(),
      applicationHomeDirectory,
      new FilesReadWriteAdapter(),
      Clock.systemDefaultZone()
    );
  }

  @Binds
  @Singleton
  abstract QueueRepository provideQueryRepository(QueueRepositoryImpl value);
}
