package com.bytedompteur.documentfinder.ui.systemtray.dagger;

import com.bytedompteur.documentfinder.commands.ExitApplicationCommand;
import com.bytedompteur.documentfinder.ui.WindowManager;
import com.bytedompteur.documentfinder.ui.adapter.out.JavaFxPlatformAdapter;
import com.bytedompteur.documentfinder.ui.systemtray.SystemTrayIconController;
import com.bytedompteur.documentfinder.ui.systemtray.SystemTrayImageFactory;
import com.bytedompteur.documentfinder.ui.systemtray.SystemTrayMenuController;
import com.jthemedetecor.OsThemeDetector;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class SystemTrayModule {

  @Provides
  static SystemTrayImageFactory provideSystemTrayImageFactory(JavaFxPlatformAdapter platformAdapter) {
    return new SystemTrayImageFactory(platformAdapter, OsThemeDetector.getDetector());
  }

  @Provides
  static SystemTrayMenuController provideSystemTrayMenuController(
    Lazy<WindowManager> windowManager,
    ExitApplicationCommand exitApplicationCommand,
    JavaFxPlatformAdapter platformAdapter
  ) {
    return new SystemTrayMenuController(windowManager, exitApplicationCommand, platformAdapter);
  }

  @Provides
  static SystemTrayIconController provideSystemTrayIconController(
    SystemTrayImageFactory factory,
    SystemTrayMenuController menuController,
    JavaFxPlatformAdapter platformAdapter
  ) {
    return new SystemTrayIconController(factory, menuController, platformAdapter);
  }
}
