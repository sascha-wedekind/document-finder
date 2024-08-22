package com.bytedompteur.documentfinder.ui.dagger;

import com.bytedompteur.documentfinder.CustomNamePrefixThreadFactoryBuilder;
import com.bytedompteur.documentfinder.PathUtil;
import com.bytedompteur.documentfinder.commands.dagger.CommandsModule;
import com.bytedompteur.documentfinder.settings.dagger.SettingsModule;
import com.bytedompteur.documentfinder.ui.WindowManager;
import com.bytedompteur.documentfinder.ui.adapter.out.JavaFxPlatformAdapter;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowComponent;
import com.bytedompteur.documentfinder.ui.optionswindow.dagger.OptionsWindowComponent;
import com.bytedompteur.documentfinder.ui.systemtray.dagger.SystemTrayComponent;
import dagger.Module;
import dagger.Provides;
import javafx.stage.Stage;
import org.cryptomator.integrations.tray.TrayIntegrationProvider;

import jakarta.inject.Named;
import jakarta.inject.Singleton;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Module(
  subcomponents = {MainWindowComponent.class, OptionsWindowComponent.class, SystemTrayComponent.class},
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
  static ExecutorService provideExecutorService(@Named("numberOfVirtualThreads") int numberOfThreads, ThreadFactory threadFactory) {
    return Executors.newFixedThreadPool(Math.max(1, numberOfThreads), threadFactory);
  }

  @Provides
  @Singleton
  static Optional<TrayIntegrationProvider> provideTrayIntegrationProvider() {
    return TrayIntegrationProvider.get();
  }

  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  @Provides
  @Singleton
  static WindowManager provideWindowManager(
    MainWindowComponent.Builder mainWindowComponentBuilder,
    OptionsWindowComponent.Builder optionsWindowComponentBuilder,
    SystemTrayComponent.Builder systemTrayComponentBuilder,
    @Named("primaryStage") Stage stage,
    JavaFxPlatformAdapter platformAdapter,
    Optional<TrayIntegrationProvider> trayIntegrationProvider
  ) {
    return new WindowManager(
      stage,
      mainWindowComponentBuilder,
      optionsWindowComponentBuilder,
      systemTrayComponentBuilder,
      platformAdapter,
      WindowManager.DEFAULT_SCENE_FACTORY,
      trayIntegrationProvider
    );
  }

  @Provides
  @Singleton
  static ThreadFactory provideThreadFactory() {
    return new CustomNamePrefixThreadFactoryBuilder().build();
  }
}
