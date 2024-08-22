package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.ui.adapter.out.FileSystemAdapter;
import com.bytedompteur.documentfinder.ui.adapter.out.JavaFxPlatformAdapter;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowScope;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.ClipboardContent;
import lombok.extern.slf4j.Slf4j;

import jakarta.inject.Inject;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Slf4j
@MainWindowScope
public class SearchResultTableContextMenu extends ContextMenu {

  private final MenuItem openFilesWithDefaultApplicationMenuItem;
  private final MenuItem openDirectoriesContainingFileMenuItem;
  private final MenuItem copyFilesToClipbordMenuItem;
  private final MenuItem openDirectoriesMenuItem;
  private final MenuItem copyDirectoriesToClipboradMenuItem;
  private final ObservableList<SearchResult> selectedSearchResult = FXCollections.observableArrayList();
  private final Map<MenuItem, Consumer<List<SearchResult>>> menuItemClickHandlerByMenuItem;
  private final FileSystemAdapter fileUtil;
  private final JavaFxPlatformAdapter platformAdapter;

  @Inject
  public SearchResultTableContextMenu(FileSystemAdapter fileUtil, JavaFxPlatformAdapter platformAdapter) {
    this.fileUtil = fileUtil;
    this.platformAdapter = platformAdapter;
    openFilesWithDefaultApplicationMenuItem = new MenuItem("");
    openDirectoriesContainingFileMenuItem = new MenuItem("");
    copyFilesToClipbordMenuItem = new MenuItem("");
    openDirectoriesMenuItem = new MenuItem("");
    copyDirectoriesToClipboradMenuItem = new MenuItem("");

    menuItemClickHandlerByMenuItem = Map.of(
      openFilesWithDefaultApplicationMenuItem, this::openFilesInOperatingSystem,
      openDirectoriesContainingFileMenuItem, this::openDirectoriesContainingFile,
      copyFilesToClipbordMenuItem, this::copyFileToClipboard,
      openDirectoriesMenuItem, this::openDirectoriesInOperatingSystem,
      copyDirectoriesToClipboradMenuItem, this::copyDirectoryToClipboard
    );

    setOnAction(this::handleContextMenuItemClick);
    setOnHidden(event -> handleContextMenuHidden());
  }

  @Override
  public void show(Node anchor, Side side, double dx, double dy) {
    prepareContextMenuItems();
    super.show(anchor, side, dx, dy);
  }

  @Override
  public void show(Node anchor, double screenX, double screenY) {
    prepareContextMenuItems();
    super.show(anchor, screenX, screenY);
  }

  @SuppressWarnings("unused")
  public List<SearchResult> getSelectedSearchResult() {
    return selectedSearchResult.stream().toList();
  }

  @SuppressWarnings("unused")
  public ObservableList<SearchResult> selectedSearchResultProperty() {
    return selectedSearchResult;
  }

  public void setSelectedSearchResult(List<SearchResult> selectedSearchResult) {
    this.selectedSearchResult.addAll(selectedSearchResult.stream().toList());
  }

  protected void prepareContextMenuItems() {
    var directoriesCount = 0;
    var fileCount = 0;
    if (searchResultHasItems()) {
      for (var searchResult : selectedSearchResult) {
        if (searchResult.isDirectory()) {
          directoriesCount++;
        } else {
          fileCount++;
        }
      }
    }

    if (directoriesCount > 0) {
      applyDirectoryRelatedContextMenuItemTexts(directoriesCount);
      getItems().add(openDirectoriesMenuItem);
      getItems().add(copyDirectoriesToClipboradMenuItem);
    }

    if (fileCount > 0) {
      applyFileRelatedContextMenuItemTexts(fileCount);
      getItems().add(openFilesWithDefaultApplicationMenuItem);
      getItems().add(copyFilesToClipbordMenuItem);
      getItems().add(openDirectoriesContainingFileMenuItem);
    }

  }

  protected void handleContextMenuItemClick(ActionEvent event) {
    if (event.getTarget() instanceof MenuItem menuItemClicked) {
      menuItemClickHandlerByMenuItem.get(menuItemClicked).accept(selectedSearchResult);
    }
  }

  private void handleContextMenuHidden() {
    selectedSearchResult.clear();
    getItems().clear();
  }


  private boolean searchResultHasItems() {
    return !selectedSearchResult.isEmpty();
  }

  private void openDirectoriesInOperatingSystem(List<SearchResult> searchResults) {
    filterOnlyDirectories(searchResults)
      .map(SearchResult::getPath)
      .filter(Objects::nonNull)
      .forEach(this::openInOperatingSystem);
  }

  private void copyDirectoryToClipboard(List<SearchResult> searchResults) {
    var clipboardContent = new ClipboardContent();
    clipboardContent.putFiles(filterOnlyDirectories(searchResults).map(it -> it.getPath().toFile()).toList());
    platformAdapter.setClipboardContent(clipboardContent);
  }

  private void openFilesInOperatingSystem(List<SearchResult> result) {
    filterOnlyFiles(result)
      .map(SearchResult::getPath)
      .filter(Objects::nonNull)
      .forEach(this::openInOperatingSystem);
  }

  private void openDirectoriesContainingFile(List<SearchResult> searchResults) {
    filterOnlyFiles(searchResults)
      .map(SearchResult::getPath)
      .filter(Objects::nonNull)
      .map(Path::getParent)
      .filter(Objects::nonNull)
      .distinct()
      .forEach(this::openInOperatingSystem);
  }

  private void openInOperatingSystem(Path path) {
    fileUtil.openInOperatingSystem(path);
  }

  private void copyFileToClipboard(List<SearchResult> path) {
    var clipboardContent = new ClipboardContent();
    clipboardContent.putFiles(filterOnlyFiles(path).map(it -> it.getPath().toFile()).toList());
    platformAdapter.setClipboardContent(clipboardContent);
  }

  private static Stream<SearchResult> filterOnlyFiles(List<SearchResult> path) {
    return path.stream().filter(it -> !it.isDirectory());
  }

  private static Stream<SearchResult> filterOnlyDirectories(List<SearchResult> path) {
    return path.stream().filter(SearchResult::isDirectory);
  }

  private void applyFileRelatedContextMenuItemTexts(int numberOf) {
    String numberOfTextPart = createNumberOfTextPart(numberOf);

    var message = MessageFormat.format("Copy {0}{1,choice,1#file|2#files} to clipboard", numberOfTextPart, numberOf);
    var message2 = MessageFormat.format("Open {1,choice,1#directory|2#directories} containing {0}{1,choice,1#file|2#files}", numberOfTextPart, numberOf);
    var message3 = MessageFormat.format("Open {0}{1,choice,1#file|2#files} with {1,choice,1#|2#their }default {1,choice,1#application|2#applications}", numberOfTextPart, numberOf);

    copyFilesToClipbordMenuItem.setText(message);
    openDirectoriesContainingFileMenuItem.setText(message2);
    openFilesWithDefaultApplicationMenuItem.setText(message3);
  }

  private void applyDirectoryRelatedContextMenuItemTexts(int numberOf) {
    String numberOfTextPart = createNumberOfTextPart(numberOf);

    var message4 = MessageFormat.format("Copy {0}{1,choice,1#directory|2#directories} to clipboard", numberOfTextPart, numberOf);
    var message5 = MessageFormat.format("Open {0}{1,choice,1#directory|2#directories}", numberOfTextPart, numberOf);

    copyDirectoriesToClipboradMenuItem.setText(message4);
    openDirectoriesMenuItem.setText(message5);
  }

  private static String createNumberOfTextPart(int numberOf) {
    var numberOfMessagePart = "";
    if(numberOf > 1) {
      numberOfMessagePart = MessageFormat.format("{0,number,integer} ", numberOf);
    }
    return numberOfMessagePart;
  }
}
