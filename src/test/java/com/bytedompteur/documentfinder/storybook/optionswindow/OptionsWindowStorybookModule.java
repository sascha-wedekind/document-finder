package com.bytedompteur.documentfinder.storybook.optionswindow;

import com.bytedompteur.documentfinder.PathUtil;
import com.bytedompteur.documentfinder.commands.ClearAllCommand;
import com.bytedompteur.documentfinder.commands.StartAllCommand;
import com.bytedompteur.documentfinder.commands.StopAllCommand;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsChangedCalculator;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import com.bytedompteur.documentfinder.settings.adapter.out.FilesReadWriteAdapter;
import com.bytedompteur.documentfinder.settings.core.SettingsServiceImpl;
import com.bytedompteur.documentfinder.ui.WindowManager;
import com.bytedompteur.documentfinder.ui.optionswindow.dagger.OptionsWindowModule;
import dagger.Module;
import dagger.Provides;
import org.mockito.Mockito;

import javax.inject.Singleton;
import java.io.IOException;

@Module(includes = OptionsWindowModule.class)
public abstract class OptionsWindowStorybookModule {

  @Provides
  @Singleton
  static PathUtil providePathUtil() {
    var mockedPathUtil = Mockito.spy(new PathUtil());
    Mockito
      .when(mockedPathUtil.isDirectory(Mockito.anyString()))
      .thenReturn(true);

    return mockedPathUtil;
  }

  @Provides
  @Singleton
  static SettingsService provideSettingsService() {
    var mockedAdapter = Mockito.mock(FilesReadWriteAdapter.class);
    try {
      Mockito.when(mockedAdapter.readString(Mockito.any())).thenReturn("");
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new SettingsServiceImpl("SAMPLE_DIRECTOR_FOR_STORYBOOK", mockedAdapter);
  }

  @Provides
  @Singleton
  static StopAllCommand provideStopAllCommand() {
    return Mockito.mock(StopAllCommand.class);
  }

  @Provides
  @Singleton
  static StartAllCommand provideStartAllCommand() {
    return Mockito.mock(StartAllCommand.class);
  }

  @Provides
  @Singleton
  static ClearAllCommand provideClearAllCommand() {
    return Mockito.mock(ClearAllCommand.class);
  }

  @Provides
  @Singleton
  static WindowManager provideWindowManager() {
    return Mockito.mock(WindowManager.class);
  }

  @Provides
  @Singleton
  static SettingsChangedCalculator provideSettingsChangedCalculator() {
    return Mockito.mock(SettingsChangedCalculator.class);
  }
}
