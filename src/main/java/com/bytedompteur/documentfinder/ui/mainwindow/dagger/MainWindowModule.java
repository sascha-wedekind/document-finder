package com.bytedompteur.documentfinder.ui.mainwindow.dagger;

import com.bytedompteur.documentfinder.ui.adapter.out.FileSystemAdapter;
import com.bytedompteur.documentfinder.ui.MacOsFileIconProvider;
import com.bytedompteur.documentfinder.ui.WindowsLinuxFileIconProvider;
import dagger.Module;
import dagger.Provides;
import javafx.application.HostServices;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.SystemUtils;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Module(includes = {BaseMainWindowModule.class})
@SuppressWarnings("java:S1610") // Dagger requires abstract class for @Provides annotation
public abstract class MainWindowModule {

  @Provides
  static FileSystemAdapter provideFileSystemAdapter(HostServices hostServices) {
    var systemFileIconProvider = SystemUtils.IS_OS_MAC ? new MacOsFileIconProvider() : new WindowsLinuxFileIconProvider();
    return new FileSystemAdapter(hostServices, systemFileIconProvider);
  }
}
