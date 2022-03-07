package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.optionswindow.dagger.OptionsWindowScope;
import dagger.Lazy;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.Map;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@OptionsWindowScope
@Slf4j
public class OptionsWindowController implements FxController {
  private final SettingsService settingsService;
  private final Lazy<Map<OptionsView.Name, OptionsView>> lazyOptionViewsByNameMap;

  private Settings settings;
  private Map<OptionsView.Name, OptionsView> optionViewsByNameMap;
  private OptionsView currentView;

  @FXML
  protected BorderPane optionsContentPane;

  @FXML
  protected void initialize() {
    settings = settingsService.read().orElse(settingsService.getDefaultSettings());
    optionViewsByNameMap = lazyOptionViewsByNameMap.get();
    showView(OptionsView.Name.FILE_TYPES_VIEW);
  }

  public void handleFileTypeOptionsSectionClick(ActionEvent ignore) {
    showView(OptionsView.Name.FILE_TYPES_VIEW);
  }

  public void handleFolderOptionsSectionClick(ActionEvent ignore) {
    showView(OptionsView.Name.FOLDER_VIEW);
  }

  public void handleCommonOptionsSectionClick(ActionEvent ignore) {
    System.out.println();
    var pane = new Pane();
    pane.setStyle("-fx-background-color: green;");
    optionsContentPane.setCenter(pane);
  }

  protected void showView(OptionsView.Name viewName) {
    if (nonNull(currentView) && viewName == currentView.getName()) {
      return;
    }

    if (nonNull(currentView)) {
      switch (currentView.getName()) {
        case FOLDER_VIEW -> hideFolderOptionsView();
        case FILE_TYPES_VIEW -> hideFileTypeOptionsView();
      }
    }

    switch (viewName) {
      case FOLDER_VIEW -> showFolderOptionsView();
      case FILE_TYPES_VIEW -> showFileTypeOptionsView();
    }
  }

  private void showFileTypeOptionsView() {
    var view = optionViewsByNameMap.get(OptionsView.Name.FILE_TYPES_VIEW);
    ((FileTypeOptionsController) view.getController()).addToFileTypesListIfNotAlreadyContained(settings.getFileTypes().toArray(new String[0]));
    optionsContentPane.setCenter(view.getViewInstance());
    setCurrentView(view);
  }

  private void showFolderOptionsView() {
    OptionsView view = optionViewsByNameMap.get(OptionsView.Name.FOLDER_VIEW);
    ((FolderOptionsController) view.getController()).addToPathListIfNotAlreadyContained(settings.getFolders().toArray(new String[0]));
    optionsContentPane.setCenter(view.getViewInstance());
    setCurrentView(view);
  }

  private void setCurrentView(OptionsView view) {
    if (nonNull(currentView)) {
      currentView.getController().beforeViewHide();
    }

    var optionsController = (OptionsController) view.getController();
    optionsController.cancelButtonClicked().subscribe(this::cancelButtonClicked);
    optionsController.okButtonClicked().subscribe(this::okButtonClicked);

    currentView = view;
  }

  private void hideFileTypeOptionsView() {
    OptionsView view = optionViewsByNameMap.get(OptionsView.Name.FILE_TYPES_VIEW);
    settings = settings
      .toBuilder()
      .fileTypes(((FileTypeOptionsController) view.getController()).getFileTypesList())
      .build();
  }

  private void okButtonClicked(Object unused) {
    log.info("OK button clicked in {}", currentView.getName());
  }

  private void cancelButtonClicked(Object unused) {
    log.info("CANCEL button clicked in {}", currentView.getName());
  }

  private void hideFolderOptionsView() {
    OptionsView view = optionViewsByNameMap.get(OptionsView.Name.FOLDER_VIEW);
    settings = settings
      .toBuilder()
      .folders(((FolderOptionsController) view.getController()).getPathsList())
      .build();
  }
}
