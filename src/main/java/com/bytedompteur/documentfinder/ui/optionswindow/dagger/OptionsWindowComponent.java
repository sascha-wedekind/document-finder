package com.bytedompteur.documentfinder.ui.optionswindow.dagger;

import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.dagger.FxmlParent;
import dagger.Component;
import dagger.Lazy;
import javafx.scene.Parent;

import javax.inject.Singleton;

@Singleton
@Component(modules = OptionsWindowModule.class)
@OptionsWindowScope
public interface OptionsWindowComponent {

  @FxmlParent(FxmlFile.OPTIONS_VIEW)
  Lazy<Parent> optionsViewNode();

  @FxmlParent(FxmlFile.FILE_TYPE_OPTIONS)
  Lazy<Parent> fileTypeOptionsView();

  @FxmlParent(FxmlFile.FOLDER_OPTIONS)
  Lazy<Parent> folderOptionsView();
}
