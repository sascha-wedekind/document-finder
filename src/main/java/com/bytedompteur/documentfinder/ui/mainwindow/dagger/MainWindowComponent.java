package com.bytedompteur.documentfinder.ui.mainwindow.dagger;

import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.dagger.FxmlParent;
import dagger.Lazy;
import dagger.Subcomponent;
import javafx.scene.Parent;

@Subcomponent(modules = MainWindowModule.class)
@MainWindowScope
public interface MainWindowComponent {

  @FxmlParent(FxmlFile.MAIN_VIEW)
  Lazy<Parent> mainViewNode();

  @Subcomponent.Builder
  interface Builder {
    MainWindowComponent build();
  }
}
