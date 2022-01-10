package com.bytedompteur.documentfinder.storybook.mainwindow;

import com.bytedompteur.documentfinder.ui.FileSystemAdapter;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.BaseMainWindowModule;
import dagger.Module;
import dagger.Provides;
import org.mockito.Mockito;

@Module(includes = BaseMainWindowModule.class)
public abstract class MainWindowStorybookModule {

  @Provides
  static FileSystemAdapter provideFileSystemAdapter() {
    return Mockito.mock(FileSystemAdapter.class);
  }

}
