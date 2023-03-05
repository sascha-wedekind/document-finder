package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import javafx.scene.Parent;

public class GeneralOptionsViewHelper extends OptionsViewHelper {

  private final GeneralOptionsController controller;

  public GeneralOptionsViewHelper(Parent viewInstance, GeneralOptionsController controller) {
    super(viewInstance, Name.FILE_TYPES_VIEW, controller);
    this.controller = controller;
  }

  void insertSettingsInController(Settings settings) {
    controller.setIsDebugLoggingEnabled(settings.isDebugLoggingEnabled());
    controller.setRunOnStartup(settings.isRunOnStartup());
  }

  Settings extractSettingsFromController(Settings settings) {
    return settings
      .toBuilder()
      .debugLoggingEnabled(controller.isDebugLoggingEnabled())
      .runOnStartup(controller.isRunOnStartup())
      .build();
  }
}
