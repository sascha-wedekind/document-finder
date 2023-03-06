package com.bytedompteur.documentfinder.settings.adapter.in;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SettingsChangedCalculatorTest {

  private SettingsChangedCalculator sut = new SettingsChangedCalculator();


  @Test
  void calculateChanges_returnsEmptySet_whenBooleanSettingsArentChanged() {
    var oldSettings = Settings.builder()
      .debugLoggingEnabled(true)
      .runOnStartup(true)
      .build();

    var newSettings = Settings.builder()
      .debugLoggingEnabled(true)
      .runOnStartup(true)
      .build();

    var result = sut.calculateChanges(oldSettings, newSettings);

    assertThat(result).isEmpty();
  }

  @Test
  void calculateChanges_returnsSetContainingDebugLogEnabled_whenBooleanSettingHasChangedChanged() {
    var oldSettings = Settings.builder()
      .debugLoggingEnabled(true)
      .build();

    var newSettings = Settings.builder()
      .debugLoggingEnabled(false)
      .build();

    var result = sut.calculateChanges(oldSettings, newSettings);

    assertThat(result).containsExactly(SettingsChangedCalculator.ChangeType.DEBUG_LOGGING_ENABLED);
  }

  @Test
  void calculateChanges_returnsSetContainingRunOnStartup_whenBooleanSettingHasChangedChanged() {
    var oldSettings = Settings.builder()
      .runOnStartup(true)
      .build();

    var newSettings = Settings.builder()
      .runOnStartup(false)
      .build();

    var result = sut.calculateChanges(oldSettings, newSettings);

    assertThat(result).containsExactly(SettingsChangedCalculator.ChangeType.RUN_ON_STARTUP);
  }

  @Test
  void calculateChanges_returnsEmptySet_whenListSettingsArentChanged() {
    var oldSettings = Settings.builder()
      .fileTypes(List.of("txt", "pdf"))
      .folders(List.of("/Users/Documents"))
      .build();

    var newSettings = Settings.builder()
      .fileTypes(List.of("txt", "pdf"))
      .folders(List.of("/Users/Documents"))
      .build();

    var result = sut.calculateChanges(oldSettings, newSettings);

    assertThat(result).isEmpty();
  }

  @Test
  void calculateChanges_returnsSetContainingFileTypes_whenFileTypesListChanged() {
    var oldSettings = Settings.builder()
      .fileTypes(List.of("txt", "pdf"))
      .build();

    var newSettings = Settings.builder()
      .fileTypes(List.of("pdf"))
      .build();

    var result = sut.calculateChanges(oldSettings, newSettings);

    assertThat(result).containsExactlyInAnyOrder(
      SettingsChangedCalculator.ChangeType.FILE_TYPES
    );
  }


  @Test
  void calculateChanges_returnsSetContainingFolder_whenFolderListChanged() {
    var oldSettings = Settings.builder()
      .folders(List.of("/Users/Documents"))
      .build();

    var newSettings = Settings.builder()
      .folders(List.of("/Users/Aws"))
      .build();

    var result = sut.calculateChanges(oldSettings, newSettings);

    assertThat(result).containsExactlyInAnyOrder(
      SettingsChangedCalculator.ChangeType.FOLDERS
    );
  }
}