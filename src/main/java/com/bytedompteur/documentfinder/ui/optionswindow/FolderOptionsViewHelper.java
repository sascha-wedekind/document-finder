package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import javafx.scene.Parent;

public class FolderOptionsViewHelper extends OptionsViewHelper {

  private final FolderOptionsController controller;

  public FolderOptionsViewHelper(Parent viewInstance, FolderOptionsController controller) {
    super(viewInstance, Name.FOLDER_VIEW, controller);
    this.controller = controller;
  }

  void insertSettingsInController(Settings settings) {
    controller.addToPathListIfNotAlreadyContained(settings.getFolders().toArray(new String[0]));
  }

  Settings extractSettingsFromController(Settings settings) {
    return settings
      .toBuilder()
      .folders(controller.getPathsList())
      .build();
  }
}
