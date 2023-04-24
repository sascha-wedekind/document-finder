package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.commands.ApplyLogLevelFromSettingsCommand;
import com.bytedompteur.documentfinder.commands.ClearAllCommand;
import com.bytedompteur.documentfinder.commands.StartAllCommand;
import com.bytedompteur.documentfinder.commands.StopAllCommand;
import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsChangedCalculator;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.UITestInitHelper;
import com.bytedompteur.documentfinder.ui.WindowManager;
import dagger.Lazy;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
class OptionsWindowControllerTest {

  private OptionsWindowController sut;

  @Mock
  SettingsService mockedSettingsService;
  @Mock
  StopAllCommand mockedStopAllCommand;
  @Mock
  StartAllCommand mockdStartAllCommand;
  @Mock
  ClearAllCommand mockedClearAllCommand;
  @Mock
  WindowManager mockedWindowManager;
  @Mock
  ApplyLogLevelFromSettingsCommand mockedApplyLogLevelsCommand;

  @Start
  void start(Stage stage) {
    Lazy<Map<OptionsViewHelper.Name, OptionsViewHelper>> lazyViewsByNameMap = () -> Map.of(
      OptionsViewHelper.Name.FILE_TYPES_VIEW, new MockedOptionsViewHelper(OptionsViewHelper.Name.FILE_TYPES_VIEW),
      OptionsViewHelper.Name.FOLDER_VIEW, new MockedOptionsViewHelper(OptionsViewHelper.Name.FOLDER_VIEW),
      OptionsViewHelper.Name.ABOUT_VIEW, new MockedOptionsViewHelper(OptionsViewHelper.Name.ABOUT_VIEW),
      OptionsViewHelper.Name.GENERAL_OPTIONS_VIEW, new MockedOptionsViewHelper(OptionsViewHelper.Name.GENERAL_OPTIONS_VIEW)
    );

    when(mockedSettingsService.read()).thenReturn(Optional.of(Settings.builder()
        .debugLoggingEnabled(false)
        .runOnStartup(false)
        .fileTypes(List.of())
        .folders(List.of())
        .build())
    );

    sut = new OptionsWindowController(
      mockedSettingsService,
      lazyViewsByNameMap,
      mockedStopAllCommand,
      mockdStartAllCommand,
      mockedClearAllCommand,
      mockedWindowManager,
      new SettingsChangedCalculator(),
      mockedApplyLogLevelsCommand
    );

    UITestInitHelper.addNodeUnderTestToStage(FxmlFile.OPTIONS_VIEW, sut, stage);
  }

  @Test
  void afterViewShown_showsFileTypesOption_asInitialView(FxRobot robot) {
    robot.interact(() -> {
      // Act
      sut.afterViewShown();

      // Assert
      assertThat(sut.getCurrentView())
        .extracting(OptionsViewHelper::getName)
        .isEqualTo(OptionsViewHelper.Name.FILE_TYPES_VIEW);
    });
  }

  @Test
  void handleAboutSectionClick_showsAboutView(FxRobot robot) {
    robot.interact(() -> {
      // Act
      sut.handleAboutSectionClick(null);

      // Assert
      assertThat(sut.getCurrentView())
        .extracting(OptionsViewHelper::getName)
        .isEqualTo(OptionsViewHelper.Name.ABOUT_VIEW);
    });
  }

  @Test
  void handleAboutSectionClick_showsFolderView(FxRobot robot) {
    robot.interact(() -> {
      // Act
      sut.handleFolderOptionsSectionClick(null);

      // Assert
      assertThat(sut.getCurrentView())
        .extracting(OptionsViewHelper::getName)
        .isEqualTo(OptionsViewHelper.Name.FOLDER_VIEW);
    });
  }

  @Test
  void handleAboutSectionClick_showsFileTypesView(FxRobot robot) {
    robot.interact(() -> {
      // Act
      sut.handleFileTypeOptionsSectionClick(null);

      // Assert
      assertThat(sut.getCurrentView())
        .extracting(OptionsViewHelper::getName)
        .isEqualTo(OptionsViewHelper.Name.FILE_TYPES_VIEW);
    });
  }

  @Test
  void handleAboutSectionClick_showsGeneralOptionsView(FxRobot robot) {
    robot.interact(() -> {
      // Act
      sut.handleGeneralOptionsSectionClick(null);

      // Assert
      assertThat(sut.getCurrentView())
        .extracting(OptionsViewHelper::getName)
        .isEqualTo(OptionsViewHelper.Name.GENERAL_OPTIONS_VIEW);
    });
  }

  @Test
  void okButtonClickedHandler_saveSettings_whenSettingsHaveChanged(FxRobot robot) {
    robot.interact(() -> {
      // Note: Controller fetches settings on initialization
      // Arrange
      sut.afterViewShown(); // ensure current view is set
      MockedOptionsViewHelper mockedVH = (MockedOptionsViewHelper) sut.getCurrentView();
      mockedVH.insertModificationContextInController(new OptionsModificationContext(
        Settings.builder().runOnStartup(true).build(),
        false
      ));

      // Act
      mockedVH.getOkCancelButtonHandler().emitOkButtonClicked();

      // Assert
      verify(mockedSettingsService).save(any());
      verify(mockedWindowManager).showMainWindow();
      verify(mockedApplyLogLevelsCommand, never()).run();
      verify(mockedStopAllCommand, never()).run();
      verify(mockedClearAllCommand, never()).run();
      verify(mockdStartAllCommand, never()).run();
    });
  }

  @Test
  void okButtonClickedHandler_rebuildIndex_whenSettingChangedThatRequireFileIndexing(FxRobot robot) {
    robot.interact(() -> {
      // Note: Controller fetches settings on initialization
      // Arrange
      sut.afterViewShown(); // ensure current view is set
      MockedOptionsViewHelper mockedVH = (MockedOptionsViewHelper) sut.getCurrentView();
      mockedVH.insertModificationContextInController(new OptionsModificationContext(
        Settings.builder().fileTypes(List.of("extensionABC")).build(),
        false
      ));

      // Act
      mockedVH.getOkCancelButtonHandler().emitOkButtonClicked();

      // Assert
      verify(mockedSettingsService).save(any()); // save settings because file types have changed
      verify(mockedApplyLogLevelsCommand, never()).run();
      verify(mockedStopAllCommand).run();
      verify(mockedClearAllCommand).run();
      verify(mockdStartAllCommand).run();
      verify(mockedWindowManager).showMainWindow();
    });
  }

  @Test
  void okButtonClickedHandler_changeLogLevel_whenLogLevelSettingChanged(FxRobot robot) {
    robot.interact(() -> {
      // Note: Controller fetches settings on initialization
      // Arrange
      sut.afterViewShown(); // ensure current view is set
      MockedOptionsViewHelper mockedVH = (MockedOptionsViewHelper) sut.getCurrentView();
      mockedVH.insertModificationContextInController(new OptionsModificationContext(
        Settings.builder().debugLoggingEnabled(true).build(),
        false
      ));

      // Act
      mockedVH.getOkCancelButtonHandler().emitOkButtonClicked();

      // Assert
      verify(mockedSettingsService).save(any()); // save settings because file log level has changed
      verify(mockedApplyLogLevelsCommand).run();
      verify(mockedStopAllCommand, never()).run();
      verify(mockedClearAllCommand, never()).run();
      verify(mockdStartAllCommand, never()).run();
      verify(mockedWindowManager).showMainWindow();
    });
  }

  @Test
  void cancelButtonClickedHandler_onlyShowsMainWindowAndDoOtherThingsOkHandlerDoes(FxRobot robot) {
    robot.interact(() -> {
      // Note: Controller fetches settings on initialization
      // Arrange
      sut.afterViewShown(); // ensure current view is set
      MockedOptionsViewHelper mockedVH = (MockedOptionsViewHelper) sut.getCurrentView();

      // Act
      mockedVH.getOkCancelButtonHandler().emitCancelButtonClicked();

      // Assert
      verify(mockedSettingsService, never()).save(any());
      verify(mockedApplyLogLevelsCommand, never()).run();
      verify(mockedStopAllCommand, never()).run();
      verify(mockedClearAllCommand, never()).run();
      verify(mockdStartAllCommand, never()).run();
      verify(mockedWindowManager).showMainWindow();
    });
  }
}