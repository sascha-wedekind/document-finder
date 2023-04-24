package com.bytedompteur.documentfinder.ui.optionswindow;

import javafx.scene.Parent;

public class AboutViewHelper extends OptionsViewHelper {

  public AboutViewHelper(Parent viewInstance, OptionsController controller) {
    super(viewInstance, Name.ABOUT_VIEW, controller);
  }

  @Override
  void insertModificationContextInController(OptionsModificationContext ignore) {
    // ignore
  }

  @Override
  OptionsModificationContext extractModificationContextFromController(OptionsModificationContext context) {
    return context;
  }
}
