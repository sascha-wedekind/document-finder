package com.bytedompteur.documentfinder.ui.mainwindow.dagger;

import com.bytedompteur.documentfinder.ui.FileSystemAdapter;
import dagger.Module;
import dagger.Provides;

@Module(includes = {BaseMainWindowModule.class})
public abstract class MainWindowModule {

  @Provides
  static FileSystemAdapter provideFileSystemAdapter() {
    return new FileSystemAdapter();
  }
}
