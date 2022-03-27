package com.bytedompteur.documentfinder;

import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Module
public class ApplicationModule {

  @Provides
  @Singleton
  public ExecutorService provideExecutorService(@Named("numberOfThreads") int numberOfThreads) {
    var maxPoolSize = numberOfThreads > 0 ? numberOfThreads : Integer.MAX_VALUE;
    return new ThreadPoolExecutor(0, maxPoolSize,
      10L, TimeUnit.SECONDS,
      new SynchronousQueue<>());
  }

  @Provides
  @Singleton
  public PathUtil providePathUtil() {
    return new PathUtil();
  }
}
