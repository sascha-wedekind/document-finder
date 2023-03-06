package com.bytedompteur.documentfinder.settings.core;

import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsChangedCalculator;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import com.bytedompteur.documentfinder.settings.adapter.out.PlatformAdapter;
import org.cryptomator.integrations.autostart.ToggleAutoStartFailedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class RunOnStartupSettingsserviceTest {

  @Mock
  PlatformAdapter mockedPlatformAdapter;

  @Mock
  SettingsService mockedSettingsService;

  @Spy
  SettingsChangedCalculator settingsChangedCalculator = new SettingsChangedCalculator();

  @InjectMocks
  RunOnStartupSettingsservice sut;

  @Test
  void isRunOnStartupSupported_returnsTrue_whenPlatformAdapterIsMacOs() {
    // Arrange
    when(mockedPlatformAdapter.isMacOs()).thenReturn(true);

    // Act
    var result = sut.isRunOnStartupSupported();

    // Assert
    assertThat(result).isTrue();
  }

  @Test
  void isRunOnStartupSupported_returnsTrue_whenPlatformAdapterIsWindows() {
    // Arrange
    when(mockedPlatformAdapter.isWindowsOs()).thenReturn(true);
    when(mockedPlatformAdapter.isMacOs()).thenReturn(false);

    // Act
    var result = sut.isRunOnStartupSupported();

    // Assert
    assertThat(result).isTrue();
  }

  @Test
  void isRunOnStartupSupported_returnsFalse_whenPlatformAdapterIsNotWindowsNorMacOs() {
    // Arrange
    when(mockedPlatformAdapter.isWindowsOs()).thenReturn(false);
    when(mockedPlatformAdapter.isMacOs()).thenReturn(false);

    // Act
    var result = sut.isRunOnStartupSupported();

    // Assert
    assertThat(result).isFalse();
  }


  @Test
  void applyRunOnStartup_passesTheValueToTheWrappedService_whenEnablingStartupOnThePlatformAdapterThrowsAnException() throws ToggleAutoStartFailedException {
    // Arrange
    when(mockedPlatformAdapter.isMacOs()).thenReturn(true);
    when(mockedPlatformAdapter.isRunOnStartupEnabled()).thenReturn(false);
    doThrow(new RuntimeException("Test exception")).when(mockedPlatformAdapter).enableRunOnStartup();
    var settingsToSave = Settings.builder().runOnStartup(true).build();
    var settingsReturnedByWrappedService = Settings.builder().build();
    when(mockedSettingsService.read()).thenReturn(Optional.of(settingsReturnedByWrappedService));

    // Act
    sut.read(); // First we have to set the inner state of the service
    sut.save(settingsToSave);

    // Assert
    verify(mockedSettingsService).save(settingsToSave);
  }

  @Test
  void applyRunOnStartup_passesTheValueToTheWrappedService_whenDisablingStartupOnThePlatformAdapterThrowsAnException() throws ToggleAutoStartFailedException {
    // Arrange
    when(mockedPlatformAdapter.isMacOs()).thenReturn(true);
    when(mockedPlatformAdapter.isRunOnStartupEnabled()).thenReturn(true);
    doThrow(new RuntimeException("Test exception")).when(mockedPlatformAdapter).disableRunOnStartup();
    var settingsToSave = Settings.builder().runOnStartup(false).build();
    var settingsReturnedByWrappedService = Settings.builder().build();
    when(mockedSettingsService.read()).thenReturn(Optional.of(settingsReturnedByWrappedService));

    // Act
    sut.read(); // First we have to set the inner state of the service
    sut.save(settingsToSave);

    // Assert
    verify(mockedSettingsService).save(settingsToSave);
  }

  @Test
  void read_overwritesRunOnStartupSettingFromPlatformAdapterBeforeReturningTheResult() {
    // Arrange
    when(mockedPlatformAdapter.isMacOs()).thenReturn(true);
    when(mockedPlatformAdapter.isRunOnStartupEnabled()).thenReturn(true);
    var settingsReturnedByWrappedService = Settings.builder().runOnStartup(false).build();
    when(mockedSettingsService.read()).thenReturn(Optional.of(settingsReturnedByWrappedService));

    // Act
    var result = sut.read();

    // Assert
    assertThat(result).isPresent().hasValueSatisfying(it -> assertThat(it.isRunOnStartup()).isTrue());
  }
}