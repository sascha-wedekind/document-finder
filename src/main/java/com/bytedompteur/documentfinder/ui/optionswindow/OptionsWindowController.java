package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.commands.ApplyLogLevelFromSettingsCommand;
import com.bytedompteur.documentfinder.commands.ClearAllCommand;
import com.bytedompteur.documentfinder.commands.StartAllCommand;
import com.bytedompteur.documentfinder.commands.StopAllCommand;
import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsChangedCalculator;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.WindowManager;
import com.bytedompteur.documentfinder.ui.optionswindow.dagger.OptionsWindowScope;
import dagger.Lazy;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@OptionsWindowScope
@Slf4j
@SuppressWarnings("java:S1172")
public class OptionsWindowController implements FxController {
  private final SettingsService settingsService;
  private final Lazy<Map<OptionsViewHelper.Name, OptionsViewHelper>> lazyOptionViewsByNameMap;
  private final StopAllCommand stopAllCommand;
  private final StartAllCommand startAllCommand;
  private final ClearAllCommand clearAllCommand;
  private final WindowManager windowManager;
  private final SettingsChangedCalculator settingsChangedCalculator;
  private final ApplyLogLevelFromSettingsCommand applyLogLevelFromSettingsCommand;

  private Settings settingsOnInitialization;
  private OptionsModificationContext optionsModificationContext;

  private Map<OptionsViewHelper.Name, OptionsViewHelper> optionViewsByNameMap;
  private OptionsViewHelper currentView;

  @FXML
  protected BorderPane optionsContentPane;

  @FXML
  protected void initialize() {
    settingsOnInitialization = settingsService.read().orElse(settingsService.getDefaultSettings());
    optionsModificationContext = new OptionsModificationContext(settingsOnInitialization, false);
    optionViewsByNameMap = lazyOptionViewsByNameMap.get();
  }

  @Override
  public void afterViewShown() {
    setCurrentView(optionViewsByNameMap.get(OptionsViewHelper.Name.FILE_TYPES_VIEW));
  }

  public void handleFileTypeOptionsSectionClick(ActionEvent ignore) {
    showView(OptionsViewHelper.Name.FILE_TYPES_VIEW);
  }

  public void handleFolderOptionsSectionClick(ActionEvent ignore) {
    showView(OptionsViewHelper.Name.FOLDER_VIEW);
  }

  public void handleAboutSectionClick(ActionEvent ignore) {
    showView(OptionsViewHelper.Name.ABOUT_VIEW);
  }

  public void handleGeneralOptionsSectionClick(ActionEvent ignore) {
    showView(OptionsViewHelper.Name.GENERAL_OPTIONS_VIEW);
  }

  public OptionsViewHelper getCurrentView() {
    return currentView;
  }

  protected void showView(OptionsViewHelper.Name viewName) {
    if (nonNull(currentView)) {
      if (viewName == currentView.getName()) return;
      optionsModificationContext = extractedContextFromCurrentView();
    }
    setCurrentView(optionViewsByNameMap.get(viewName));
  }

  private void setCurrentView(OptionsViewHelper view) {
    if (nonNull(currentView)) {
      currentView.beforeViewHide();
    }

    optionsContentPane.setCenter(view.getViewInstance());
    view.insertModificationContextInController(optionsModificationContext);
    view.cancelButtonClicked().subscribe(this::cancelButtonClicked);
    view.okButtonClicked().subscribe(this::okButtonClicked);
    currentView = view;
  }

  protected void okButtonClicked(Object unused) {
    var context = extractedContextFromCurrentView();
    var changedSettings = settingsChangedCalculator.calculateChanges(settingsOnInitialization, context.getSettings());

    if (!changedSettings.isEmpty()) {
      log.debug("Settings that have changed: {}", changedSettings);
      settingsService.save(context.getSettings());
    }

    if (changedSettings.contains(SettingsChangedCalculator.ChangeType.DEBUG_LOGGING_ENABLED)) {
      log.debug("Executing apply log level from settings command");
      applyLogLevelFromSettingsCommand.run();
    }

    if (context.isForceIndexRebuild() || settingChangedThatRequireFileIndexing(changedSettings)) {
      log.debug("Executing stop all command");
      stopAllCommand.run();

      log.debug("Executing clear all command");
      clearAllCommand.run();

      log.debug("Executing run all command");
      startAllCommand.run();
    }

    log.debug("OK button clicked in {}", currentView.getName());
    windowManager.showMainWindow();
  }

  protected void cancelButtonClicked(Object unused) {
    log.debug("CANCEL button clicked in {}", currentView.getName());
    windowManager.showMainWindow();
  }

  private OptionsModificationContext extractedContextFromCurrentView() {
    return Optional
      .ofNullable(currentView)
      .map(it -> it.extractModificationContextFromController(optionsModificationContext))
      .orElse(optionsModificationContext);
  }

  private static boolean settingChangedThatRequireFileIndexing(Set<SettingsChangedCalculator.ChangeType> changedSettings) {
    return changedSettings.contains(SettingsChangedCalculator.ChangeType.FILE_TYPES)
      || changedSettings.contains(SettingsChangedCalculator.ChangeType.FOLDERS);
  }
}
