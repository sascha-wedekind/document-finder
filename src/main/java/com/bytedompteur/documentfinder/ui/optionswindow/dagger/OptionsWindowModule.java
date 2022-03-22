package com.bytedompteur.documentfinder.ui.optionswindow.dagger;

import com.bytedompteur.documentfinder.PathUtil;
import com.bytedompteur.documentfinder.commands.dagger.CommandsModule;
import com.bytedompteur.documentfinder.settings.dagger.SettingsModule;
import dagger.Module;
import dagger.Provides;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Module(includes = {BaseOptionsWindowModule.class, SettingsModule.class, CommandsModule.class})
public abstract class OptionsWindowModule {

  @Provides
  @Singleton
  public static PathUtil providePathUtil() {
    return new PathUtil();
  }

  // sascha: Delete when window system is connected with application or vice versa
  @Provides
  @Singleton
  static ExecutorService provideExecutorService(@Named("numberOfThreads") int numberOfThreads) {
    return Executors.newFixedThreadPool(Math.max(1, numberOfThreads));
  }
}
