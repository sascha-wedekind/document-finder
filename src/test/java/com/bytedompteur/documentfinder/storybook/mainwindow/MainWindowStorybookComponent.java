package com.bytedompteur.documentfinder.storybook.mainwindow;


import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowComponent;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowScope;
import dagger.BindsInstance;
import dagger.Component;

import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Component(modules = MainWindowStorybookModule.class)
@Singleton
@MainWindowScope
public interface MainWindowStorybookComponent extends MainWindowComponent {

  @Component.Builder
  interface Builder {
    @BindsInstance
    MainWindowStorybookComponent.Builder numberOfThreads(@Named("numberOfThreads") int value);

    @BindsInstance
    MainWindowStorybookComponent.Builder applicationHomeDirectory(@Named("applicationHomeDirectory") String value);

    MainWindowStorybookComponent build();
  }

}
