package com.bytedompteur.documentfinder.ui.optionswindow;

import javafx.scene.Parent;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class FileTypeOptionsViewHelper extends OptionsViewHelper {

  private final FileTypeOptionsController controller;

  public FileTypeOptionsViewHelper(Parent viewInstance, FileTypeOptionsController controller) {
    super(viewInstance, Name.FILE_TYPES_VIEW, controller);
    this.controller = controller;
  }

  void insertModificationContextInController(OptionsModificationContext context) {
    controller.addToFileTypesListIfNotAlreadyContained(context.getSettings().getFileTypes().toArray(new String[0]));
  }

  OptionsModificationContext extractModificationContextFromController(OptionsModificationContext context) {
    var settings = context.getSettings().withFileTypes(controller.getFileTypesList());
    return context.withSettings(settings);
  }
}
