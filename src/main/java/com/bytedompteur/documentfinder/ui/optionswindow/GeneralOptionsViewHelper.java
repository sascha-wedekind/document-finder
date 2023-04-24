package com.bytedompteur.documentfinder.ui.optionswindow;

import javafx.scene.Parent;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class GeneralOptionsViewHelper extends OptionsViewHelper {

  private final GeneralOptionsController controller;

  public GeneralOptionsViewHelper(Parent viewInstance, GeneralOptionsController controller) {
    super(viewInstance, Name.FILE_TYPES_VIEW, controller);
    this.controller = controller;
  }

  void insertModificationContextInController(OptionsModificationContext context) {
    controller.setIsDebugLoggingEnabled(context.getSettings().isDebugLoggingEnabled());
    controller.setRunOnStartup(context.getSettings().isRunOnStartup());
  }

  OptionsModificationContext extractModificationContextFromController(OptionsModificationContext context) {
    var settings = context.getSettings()
      .withDebugLoggingEnabled(controller.isDebugLoggingEnabled())
      .withRunOnStartup(controller.isRunOnStartup());
    return context
      .withSettings(settings)
      .withForceIndexRebuild(controller.isForceIndexRebuild());
  }
}
