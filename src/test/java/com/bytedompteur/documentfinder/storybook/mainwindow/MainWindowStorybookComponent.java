package com.bytedompteur.documentfinder.storybook.mainwindow;


import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowComponent;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowScope;
import dagger.Component;

import javax.inject.Singleton;

@Component(modules = MainWindowStorybookModule.class)
@Singleton
@MainWindowScope
public interface MainWindowStorybookComponent extends MainWindowComponent {
}
