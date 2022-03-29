package com.bytedompteur.documentfinder.ui.optionswindow.dagger;

import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.dagger.FxmlParent;
import com.bytedompteur.documentfinder.ui.optionswindow.OptionsWindowController;
import dagger.Lazy;
import dagger.Subcomponent;
import javafx.scene.Parent;

@Subcomponent(modules = {OptionsWindowModule.class})
@OptionsWindowScope
public interface OptionsWindowComponent {

  @FxmlParent(FxmlFile.OPTIONS_VIEW)
  Lazy<Parent> optionsViewNode();

  @FxmlParent(FxmlFile.FILE_TYPE_OPTIONS)
  Lazy<Parent> fileTypeOptionsView();

  @FxmlParent(FxmlFile.FOLDER_OPTIONS)
  Lazy<Parent> folderOptionsView();

  OptionsWindowController optionsWindowController();

  @Subcomponent.Builder
  interface Builder {
    OptionsWindowComponent build();
  }
}
