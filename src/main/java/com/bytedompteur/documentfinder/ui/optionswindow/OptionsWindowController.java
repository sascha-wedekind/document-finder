package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.settings.adapter.in.Settings;
import com.bytedompteur.documentfinder.settings.adapter.in.SettingsService;
import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.dagger.FxmlParent;
import com.bytedompteur.documentfinder.ui.optionswindow.dagger.OptionsWindowScope;
import dagger.Lazy;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Map;

import static java.util.Objects.nonNull;

@OptionsWindowScope
public class OptionsWindowController implements FxController {

  private final Lazy<Parent> fileTypeOptionsView;
  private final Lazy<Parent> folderOptionsView;
  private final SettingsService settingsService;
  private final Map<Class<? extends FxController>, Provider<FxController>> controllerFactoriesByClassMap;
  private Settings settings;
  private Map<OptionsView.Name, OptionsView> viewsByNameMap;
  private OptionsView currentView;

  @FXML
  public BorderPane optionsContentPane;

  @Inject
  public OptionsWindowController(
    @FxmlParent(FxmlFile.FILE_TYPE_OPTIONS) Lazy<Parent> fileTypeOptionsView,
    @FxmlParent(FxmlFile.FOLDER_OPTIONS) Lazy<Parent> folderOptionsView,
    SettingsService settingsService,
    Map<Class<? extends FxController>, Provider<FxController>> controllerFactoriesByClassMap
  ) {
    this.fileTypeOptionsView = fileTypeOptionsView;
    this.folderOptionsView = folderOptionsView;
    this.settingsService = settingsService;
    this.controllerFactoriesByClassMap = controllerFactoriesByClassMap;
  }

  @FXML
  protected void initialize() {
    settings = settingsService.read().orElse(settingsService.getDefaultSettings());

    viewsByNameMap = Map.of(
      OptionsView.Name.FILE_TYPES_VIEW, new OptionsView(fileTypeOptionsView.get(), controllerFactoriesByClassMap.get(FileTypeOptionsController.class).get(), OptionsView.Name.FILE_TYPES_VIEW),
      OptionsView.Name.FOLDER_VIEW, new OptionsView(folderOptionsView.get(), controllerFactoriesByClassMap.get(FolderOptionsController.class).get(), OptionsView.Name.FOLDER_VIEW)
    );

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
    if (nonNull(currentView)) {
      switch (currentView.getName()) {
        case FOLDER_VIEW -> {
          OptionsView view = viewsByNameMap.get(OptionsView.Name.FOLDER_VIEW);
          settings = settings
            .toBuilder()
            .folders(((FolderOptionsController) view.getController()).getPathsList())
            .build();
        }
        case FILE_TYPES_VIEW -> {
          OptionsView view = viewsByNameMap.get(OptionsView.Name.FILE_TYPES_VIEW);
          settings = settings
            .toBuilder()
            .fileTypes(((FileTypeOptionsController) view.getController()).getFileTypesList())
            .build();
        }
      }
    }

    switch (viewName) {
      case FOLDER_VIEW -> {
        OptionsView view = viewsByNameMap.get(OptionsView.Name.FOLDER_VIEW);
        ((FolderOptionsController) view.getController()).addToPathListIfNotAlreadyContained(settings.getFolders().toArray(new String[0]));
        optionsContentPane.setCenter(view.getViewInstance());
        currentView = view;
      }
      case FILE_TYPES_VIEW -> {
        var view = viewsByNameMap.get(OptionsView.Name.FILE_TYPES_VIEW);
        ((FileTypeOptionsController) view.getController()).addToFileTypesListIfNotAlreadyContained(settings.getFileTypes().toArray(new String[0]));
        optionsContentPane.setCenter(view.getViewInstance());
        currentView = view;
      }
    }
  }
}
