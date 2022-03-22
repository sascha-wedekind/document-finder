package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import javafx.scene.Parent;

public class FileTypeOptionsViewHelper extends OptionsViewHelper {

  private final FileTypeOptionsController controller;

  public FileTypeOptionsViewHelper(Parent viewInstance, FileTypeOptionsController controller) {
    super(viewInstance, Name.FILE_TYPES_VIEW, controller);
    this.controller = controller;
  }

  void insertSettingsInController(Settings settings) {
    controller.addToFileTypesListIfNotAlreadyContained(settings.getFileTypes().toArray(new String[0]));
  }

  Settings extractSettingsFromController(Settings settings) {
    return settings
      .toBuilder()
      .fileTypes(controller.getFileTypesList())
      .build();
  }
}
