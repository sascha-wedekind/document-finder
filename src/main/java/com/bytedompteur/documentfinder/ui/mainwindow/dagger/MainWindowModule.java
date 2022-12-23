package com.bytedompteur.documentfinder.ui.mainwindow.dagger;

import com.bytedompteur.documentfinder.ui.FileSystemAdapter;
import com.bytedompteur.documentfinder.ui.MacOsFileIconProvider;
import com.bytedompteur.documentfinder.ui.SystemFileIconProvider;
import com.bytedompteur.documentfinder.ui.WindowsLinuxFileIconProvider;
import dagger.Module;
import dagger.Provides;
import javafx.application.HostServices;
import org.apache.commons.lang3.SystemUtils;

@Module(includes = {BaseMainWindowModule.class})
public abstract class MainWindowModule {

  @Provides
  static FileSystemAdapter provideFileSystemAdapter(HostServices hostServices) {
    var systemFileIconProvider = SystemUtils.IS_OS_MAC ? new MacOsFileIconProvider() : new WindowsLinuxFileIconProvider();
    return new FileSystemAdapter(hostServices, systemFileIconProvider);
  }
}
