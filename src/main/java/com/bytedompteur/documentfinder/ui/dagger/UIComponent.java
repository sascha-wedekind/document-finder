package com.bytedompteur.documentfinder.ui.dagger;

import com.bytedompteur.documentfinder.ui.WindowManager;
import dagger.BindsInstance;
import dagger.Component;
import javafx.stage.Stage;

import javax.inject.Named;
import javax.inject.Singleton;

@Component(modules = UIModule.class)
@Singleton
public interface UIComponent {

  WindowManager windowManager();

  @Component.Builder
  interface Builder {
    @BindsInstance
    UIComponent.Builder applicationHomeDirectory(@Named("applicationHomeDirectory") String value);

    @BindsInstance
    UIComponent.Builder numberOfThreads(@Named("numberOfThreads") int value);

    @BindsInstance
    UIComponent.Builder primaryStage(@Named("primaryStage") Stage value);

    UIComponent build();
  }
}
