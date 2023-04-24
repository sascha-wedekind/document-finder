package com.bytedompteur.documentfinder.ui.optionswindow;

import javafx.scene.Parent;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
public class FolderOptionsViewHelper extends OptionsViewHelper {

  private final FolderOptionsController controller;

  public FolderOptionsViewHelper(Parent viewInstance, FolderOptionsController controller) {
    super(viewInstance, Name.FOLDER_VIEW, controller);
    this.controller = controller;
  }

  void insertModificationContextInController(OptionsModificationContext context) {
    controller.addToPathListIfNotAlreadyContained(context.getSettings().getFolders().toArray(new String[0]));
  }

  OptionsModificationContext extractModificationContextFromController(OptionsModificationContext context) {
    var settings = context.getSettings().withFolders(controller.getPathsList());
    return context.withSettings(settings);
  }
}
