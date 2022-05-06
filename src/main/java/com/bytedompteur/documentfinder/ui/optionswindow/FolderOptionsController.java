package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.PathUtil;
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
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@OptionsWindowScope
@SuppressWarnings("java:S1172")
public class FolderOptionsController extends BaseOptionsController {

  private final ObservableList<String> pathsList = FXCollections.observableArrayList();
  private final PathUtil pathUtil;

  @FXML
  public ListView<String> pathsListView;

  @FXML
  public TextField pathToAddTextField;

  @FXML
  public Button addToPathsListViewButton;

  @FXML
  public Button removeSelectedPathsButton;

  @Inject
  public FolderOptionsController(OkCancelButtonHandler okCancelButtonHandler, PathUtil pathUtil) {
    super(okCancelButtonHandler);
    this.pathUtil = pathUtil;
  }

  @FXML
  public void initialize() {
    pathsListView.setItems(pathsList);
    registerPathsListViewSelectionListener();
    registerPathsToAddTextFieldChangeListener();
    disableAddToPathsListViewButton();
    disableRemoveSelectedPathsButton();
  }

  public void setAddToPathsListViewButtonDisabledStateBasedOnPathsToAddTextFieldSize(int textFieldSize) {
    if (textFieldSize > 0) {
      enableAddToPathsListViewButton();
    } else {
      disableAddToPathsListViewButton();
    }
  }

  public void disableAddToPathsListViewButton() {
    addToPathsListViewButton.setDisable(true);
  }

  public void enableAddToPathsListViewButton() {
    addToPathsListViewButton.setDisable(false);
  }

  public void setRemoveSelectedPathsButtonDisabledStateBasedOnNumberOfSelections(int numberOfSelections) {
    if (numberOfSelections > 0) {
      enableRemoveSelectedPathsButton();
    } else {
      disableRemoveSelectedPathsButton();
    }
  }

  public void disableRemoveSelectedPathsButton() {
    removeSelectedPathsButton.setDisable(true);
  }

  public void enableRemoveSelectedPathsButton() {
    removeSelectedPathsButton.setDisable(false);
  }

  protected void registerPathsToAddTextFieldChangeListener() {
    pathToAddTextField
      .lengthProperty()
      .addListener((observable, oldValue, newValue) -> setAddToPathsListViewButtonDisabledStateBasedOnPathsToAddTextFieldSize(newValue.intValue()));
  }

  protected void registerPathsListViewSelectionListener() {
    pathsListView
      .getSelectionModel()
      .getSelectedIndices()
      .addListener((ListChangeListener<Integer>) c -> setRemoveSelectedPathsButtonDisabledStateBasedOnNumberOfSelections(c.getList().size()));
  }

  public void removeSelectedFromPathsListView(ActionEvent ignore) {
    var selectedIndices = pathsListView.getSelectionModel().getSelectedIndices();
    removeByIndicesFromPathsList(selectedIndices);
  }

  public void removeByIndicesFromPathsList(List<Integer> indices) {
    Mono
      .justOrEmpty(indices)
      .filter(it -> !it.isEmpty())
      .flatMapMany(Flux::fromIterable) // all selected indices
      .map(pathsList::get) // map to list value
      .doOnError(t -> log.warn("While executing 'removeByIndicesFromPathsList'.", t))
      .onErrorResume(throwable -> Mono.empty())
      .collectList()
      .subscribe(pathsList::removeAll); // remove paths from list
  }

  public void removeAllFromPathsListView(ActionEvent ignore) {
    clearPathsList();
  }

  public void clearPathsList() {
    pathsList.clear();
  }

  public void addToPathsListView(ActionEvent ignore) {
    addToPathListIfNotAlreadyContained(pathToAddTextField.getText());
  }

  public void addToPathListIfNotAlreadyContained(String... paths) {
    Flux
      .fromArray(paths)
      .subscribe(this::addToPathListIfNotAlreadyContained);
  }

  public void addToPathListIfNotAlreadyContained(String pathToAdd) {
    //noinspection ReactiveStreamsTooLongSameOperatorsChain
    Mono
      .justOrEmpty(pathToAdd)
      .defaultIfEmpty("")
      .filter(StringUtils::isNotBlank)
      .filter(this::isValidDirectoryToBeAddedToPathList)
      .filter(it -> pathIsNotChildOfAnyPathInList(it, pathsList))
      .doOnEach(ignore -> pathToAddTextField.clear())
      .filter(it -> !pathsList.contains(it))
      .subscribe(pathsList::add);
  }

  public ObservableList<String> getPathsList() {
    return FXCollections.unmodifiableObservableList(pathsList);
  }

  protected boolean pathIsNotChildOfAnyPathInList(String path, List<String> pathsList) {
    var pathsInList = pathsList
      .stream()
      .map(Path::of)
      .toList();

    var pathsPlusPathToAdd = new ArrayList<>(pathsInList);
    pathsPlusPathToAdd.add(Path.of(path));

    return !(pathUtil.removeChildPaths(pathsPlusPathToAdd).size() == pathUtil.removeChildPaths(pathsInList).size());
  }

  protected boolean isValidDirectoryToBeAddedToPathList(String value) {
    var isDirectory = pathUtil.isDirectory(value);
    if (!isDirectory) {
      log.warn("'{}' is not a an existing directory and won't be added to path list.", value);
    }
    return isDirectory;
  }

  public void handleOkButtonClick(ActionEvent ignore) {
    emitOkButtonClicked();
  }

  public void handleCancelButtonClick(ActionEvent ignore) {
    emitCancelButtonClicked();
  }
}
