package com.bytedompteur.documentfinder.ui.mainwindow.dagger;

import com.bytedompteur.documentfinder.ui.adapter.out.JnaMacOsFileIconProvider;
import com.bytedompteur.documentfinder.ui.adapter.out.FileSystemAdapter;
import com.bytedompteur.documentfinder.ui.adapter.out.WindowsLinuxFileIconProvider;
import dagger.Module;
import dagger.Provides;
import javafx.application.HostServices;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SystemUtils;

import java.awt.Desktop;
import java.util.Optional;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Module(includes = {BaseMainWindowModule.class})
@SuppressWarnings("java:S1610") // Dagger requires abstract class for @Provides annotation
public abstract class MainWindowModule {

  @Provides
  static FileSystemAdapter provideFileSystemAdapter(HostServices hostServices) {
      var systemFileIconProvider = SystemUtils.IS_OS_MAC ? JnaMacOsFileIconProvider.getInstance() : new WindowsLinuxFileIconProvider();
      return new FileSystemAdapter(
          hostServices,
          systemFileIconProvider,
          desktopSupportingOpen(),
          runnable -> Thread.ofVirtual().name("open-in-os").start(runnable)
      );
  }

  private static Optional<Desktop> desktopSupportingOpen() {
      if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
          return Optional.of(Desktop.getDesktop());
      }
      log.warn("java.awt.Desktop can't open files on this platform, falling back to JavaFX HostServices");
      return Optional.empty();
  }
}
