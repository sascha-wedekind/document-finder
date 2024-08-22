package com.bytedompteur.documentfinder.persistedqueue.dagger;

import com.bytedompteur.documentfinder.persistedqueue.adapter.in.PersistedUniqueFileEventQueue;
import com.bytedompteur.documentfinder.persistedqueue.adapter.in.QueueRepository;
import com.bytedompteur.documentfinder.persistedqueue.adapter.out.FilesReadWriteAdapter;
import com.bytedompteur.documentfinder.persistedqueue.adapter.out.SettingsServiceAdapter;
import com.bytedompteur.documentfinder.persistedqueue.core.FilteringFileEventQueueDelegate;
import com.bytedompteur.documentfinder.persistedqueue.core.PersistedQueueItemCompactor;
import com.bytedompteur.documentfinder.persistedqueue.core.PersistedUniqueFileEventQueueImpl;
import com.bytedompteur.documentfinder.persistedqueue.core.QueueRepositoryImpl;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import com.bytedompteur.documentfinder.settings.dagger.SettingsModule;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.time.Clock;

@Module(includes= SettingsModule.class)
public abstract class PersistedQueueModule {

  @Provides
  @Singleton
  static PersistedUniqueFileEventQueue provideEventQueue(PersistedUniqueFileEventQueueImpl value, SettingsServiceAdapter settingsService) {
    return new FilteringFileEventQueueDelegate(value, settingsService);
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
