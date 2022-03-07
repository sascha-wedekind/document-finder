package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.ui.optionswindow.dagger.OptionsWindowScope;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.inject.Inject;

@Slf4j
@OptionsWindowScope
public class FileTypeOptionsController extends BaseOptionsController {

  private final ObservableList<String> fileTypesList = FXCollections.observableArrayList();

  @FXML
  public ListView<String> fileTypeListView;

  @FXML
  public TextField fileTypesToAddTextField;

  @FXML
  public Button addToFileTypeListViewButton;

  @FXML
  public Button removeSelectedFileTypeButton;

  @Inject
  public FileTypeOptionsController(OkCancelButtonHandler okCancelButtonHandler) {
    super(okCancelButtonHandler);
  }

  @FXML
  public void initialize() {
    fileTypeListView.setItems(fileTypesList);
    registerFileTypeListViewSelectionListener();
    registerFileTypesToAddTextFieldChangeListener();
    disableAddToFileTypeListViewButton();
    disableRemoveSelectedFileTypeButton();
  }

  public void removeSelectedFromFileTypeListView(ActionEvent ignored) {
    var selectedIndices = fileTypeListView.getSelectionModel().getSelectedIndices();
    removeByIndicesFromFileTypeList(selectedIndices);
  }

  public void removeAllFromFileTypeListView(ActionEvent ignored) {
    clearFileTypesList();
  }

  public void addToFileTypeListView(ActionEvent ignored) {
    addToFileTypesListIfNotAlreadyContained(fileTypesToAddTextField.getText());
  }

  public void handleOkButtonClick(ActionEvent ignored) {
    emitOkButtonClicked();
  }

  public void handleCancelButtonClick(ActionEvent ignored) {
    emitCancelButtonClicked();
  }

  public void removeByIndicesFromFileTypeList(ObservableList<Integer> indices) {
    Mono
      .justOrEmpty(indices)
      .filter(it -> !it.isEmpty())
      .flatMapMany(Flux::fromIterable) // all selected indices
      .map(fileTypesList::get) // map to list value
      .doOnError(t -> log.warn("While executing 'removeByIndicesFromFileTypeList'.", t))
      .onErrorResume(throwable -> Mono.empty())
      .collectList()
      .subscribe(fileTypesList::removeAll); // remove file type from list
  }

  public void clearFileTypesList() {
    fileTypesList.clear();
  }

  public void addToFileTypesListIfNotAlreadyContained(String ...types) {
    Flux
      .fromArray(types)
      .subscribe(this::addToFileTypesListIfNotAlreadyContained);
  }

  public void addToFileTypesListIfNotAlreadyContained(String fileTypeToAdd) {
    Mono
      .justOrEmpty(fileTypeToAdd)
      .defaultIfEmpty("")
      .filter(StringUtils::isNotBlank)
      .doOnEach(ignore -> fileTypesToAddTextField.clear())
      .map(String::toLowerCase)
      .filter(it -> !fileTypesList.contains(it))
      .subscribe(fileTypesList::add);
  }

  public void setAddToFileTypeListViewButtonDisabledStateBasedOnFileTypesToAddTextFieldSize(int value) {
    if (value > 0) {
      enableAddToFileTypeListViewButton();
    } else {
      disableAddToFileTypeListViewButton();
    }
  }

  public void enableAddToFileTypeListViewButton() {
    addToFileTypeListViewButton.setDisable(false);
  }

  public void disableAddToFileTypeListViewButton() {
    addToFileTypeListViewButton.setDisable(true);
  }

  public void setRemoveSelectedFileTypeButtonDisabledStateBasedOnNumberOfSelections(int value) {
    if (value > 0) {
      enableRemoveSelectedFileTypeButton();
    } else {
      disableRemoveSelectedFileTypeButton();
    }
  }

  public void enableRemoveSelectedFileTypeButton() {
    removeSelectedFileTypeButton.setDisable(false);
  }

  public void disableRemoveSelectedFileTypeButton() {
    removeSelectedFileTypeButton.setDisable(true);
  }

  public ObservableList<String> getFileTypesList() {
    return FXCollections.unmodifiableObservableList(fileTypesList);
  }

  private void registerFileTypesToAddTextFieldChangeListener() {
    fileTypesToAddTextField
      .lengthProperty()
      .addListener((observable, oldValue, newValue) -> setAddToFileTypeListViewButtonDisabledStateBasedOnFileTypesToAddTextFieldSize(newValue.intValue()));
  }

  private void registerFileTypeListViewSelectionListener() {
    fileTypeListView
      .getSelectionModel()
      .getSelectedIndices()
      .addListener((ListChangeListener<Integer>) c -> setRemoveSelectedFileTypeButtonDisabledStateBasedOnNumberOfSelections(c.getList().size()));
  }
}
