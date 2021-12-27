package com.bytedompteur.documentfinder;

import dagger.Module;
import dagger.Provides;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.inject.Named;
import javax.inject.Singleton;

@Module
public class ApplicationModule {

  @Provides
  @Singleton
  public ExecutorService provideExecutorService(@Named("numberOfThreads") int numberOfThreads) {
    return Executors.newFixedThreadPool(numberOfThreads);
  }

}
