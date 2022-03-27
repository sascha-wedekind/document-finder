package com.bytedompteur.documentfinder.ui.dagger;

import com.bytedompteur.documentfinder.PathUtil;
import com.bytedompteur.documentfinder.commands.dagger.CommandsModule;
import com.bytedompteur.documentfinder.settings.dagger.SettingsModule;
import com.bytedompteur.documentfinder.ui.WindowManager;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowComponent;
import com.bytedompteur.documentfinder.ui.optionswindow.dagger.OptionsWindowComponent;
import dagger.Module;
import dagger.Provides;
import javafx.stage.Stage;

import javax.inject.Named;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Module(
  subcomponents = {MainWindowComponent.class, OptionsWindowComponent.class},
  includes = {SettingsModule.class, CommandsModule.class}
)
public abstract class UIModule {

  @Provides
  static MainWindowComponent provideMainWindowComponent(MainWindowComponent.Builder builder) {
    return builder.build();
  }

  @Provides
  static OptionsWindowComponent provideOptionsWindowComponent(OptionsWindowComponent.Builder builder) {
    return builder.build();
  }

  @Provides
  @Singleton
  public static PathUtil providePathUtil() {
    return new PathUtil();
  }

  @Provides
  @Singleton
  static ExecutorService provideExecutorService(@Named("numberOfThreads") int numberOfThreads) {
    return Executors.newFixedThreadPool(Math.max(1, numberOfThreads));
  }

  @Provides
  @Singleton
  static WindowManager provideWindowManager(
    MainWindowComponent.Builder mainWindowComponentBuilder,
    OptionsWindowComponent.Builder optionsWindowComponentBuilder,
    @Named("primaryStage") Stage stage
  ) {
    return new WindowManager(stage, mainWindowComponentBuilder, optionsWindowComponentBuilder);
  }
}
