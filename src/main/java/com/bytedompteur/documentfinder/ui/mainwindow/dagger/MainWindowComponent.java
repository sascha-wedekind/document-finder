package com.bytedompteur.documentfinder.ui.mainwindow.dagger;

import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.dagger.FxmlParent;
import dagger.BindsInstance;
import dagger.Component;
import dagger.Lazy;
import javafx.scene.Parent;

import javax.inject.Named;
import javax.inject.Singleton;

@Singleton
@Component(modules = MainWindowModule.class)
@MainWindowScope
public interface MainWindowComponent {

  @FxmlParent(FxmlFile.MAIN_VIEW)
  Lazy<Parent> mainViewNode();

  @Component.Builder
  interface Builder {
    @BindsInstance
    MainWindowComponent.Builder numberOfThreads(@Named("numberOfThreads") int value);
    MainWindowComponent build();
  }
}
