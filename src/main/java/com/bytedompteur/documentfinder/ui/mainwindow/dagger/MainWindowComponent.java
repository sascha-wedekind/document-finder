package com.bytedompteur.documentfinder.ui.mainwindow.dagger;

import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.dagger.FxmlParent;
import dagger.Component;
import dagger.Lazy;
import javafx.scene.Parent;

import javax.inject.Singleton;

@Singleton
@Component(modules = MainWindowModule.class)
@MainWindowScope
public interface MainWindowComponent {

  @FxmlParent(FxmlFile.MAIN_VIEW)
  Lazy<Parent> mainViewNode();

}
