package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import javafx.scene.Parent;

public class AboutViewHelper extends OptionsViewHelper {

  public AboutViewHelper(Parent viewInstance, OptionsController controller) {
    super(viewInstance, Name.ABOUT_VIEW, controller);
  }

  @Override
  void insertSettingsInController(Settings ignore) {
    // ignore
  }

  @Override
  Settings extractSettingsFromController(Settings settings) {
    return settings;
  }
}
