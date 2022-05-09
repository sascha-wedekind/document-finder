package com.bytedompteur.documentfinder.ui.mainwindow.dagger;

import com.bytedompteur.documentfinder.ui.FileSystemAdapter;
import dagger.Module;
import dagger.Provides;
import javafx.application.HostServices;

@Module(includes = {BaseMainWindowModule.class})
public abstract class MainWindowModule {

  @Provides
  static FileSystemAdapter provideFileSystemAdapter(HostServices hostServices) {
    return new FileSystemAdapter(hostServices);
  }
}
