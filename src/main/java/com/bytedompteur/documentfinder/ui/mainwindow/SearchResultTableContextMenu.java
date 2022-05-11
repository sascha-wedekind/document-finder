package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.ui.FileSystemAdapter;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowScope;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static java.util.Objects.nonNull;

@Slf4j
@MainWindowScope
public class SearchResultTableContextMenu extends ContextMenu {

  public static final String OPEN_WITH_DEFAULT_MENU_ITEM_TEXT = "Open";
  public static final String OPEN_DIRECTORY_MENU_ITEM_TEXT = "Open directory";
  public static final String COPY_FILE_MENU_ITEM_TEXT = "Copy";
  private final MenuItem openWithDefaultApplicationMenuItem;
  private final MenuItem openDirectoryContainingFileMenuItem;
  private final MenuItem copyFileToClipbordMenuItem;
  private final ObjectProperty<SearchResult> selectedSearchResult = new SimpleObjectProperty<>();
  private final Map<MenuItem, Consumer<SearchResult>> menuItemClickHandlerByMenuItem;
  private final FileSystemAdapter fileUtil;

  @Inject
  public SearchResultTableContextMenu(FileSystemAdapter fileUtil) {
    this.fileUtil = fileUtil;
    openWithDefaultApplicationMenuItem = new MenuItem(OPEN_WITH_DEFAULT_MENU_ITEM_TEXT);
    openDirectoryContainingFileMenuItem = new MenuItem(OPEN_DIRECTORY_MENU_ITEM_TEXT);
    copyFileToClipbordMenuItem = new MenuItem(COPY_FILE_MENU_ITEM_TEXT);

    menuItemClickHandlerByMenuItem = Map.of(
      openWithDefaultApplicationMenuItem, result -> openInOperatingSystem(result.getPath()),
      openDirectoryContainingFileMenuItem, result -> openInOperatingSystem(result.getPath().getParent()),
      copyFileToClipbordMenuItem, result -> copyFileToClipboard(result.getPath())
    );

    setOnAction(this::handleContextMenuItemClick);
    setOnHidden(event -> handleContextMenuHidden());
    selectedSearchResult.addListener((observable, oldValue, newValue) -> handleSelectedSearchResultChange(newValue));
  }

  protected void handleContextMenuItemClick(ActionEvent event) {
    if (event.getTarget() instanceof MenuItem menuItemClicked && searchResultIsValid()) {
      var searchResult = selectedSearchResult.get();
      menuItemClickHandlerByMenuItem.get(menuItemClicked).accept(searchResult);
    }
  }

  protected void handleContextMenuHidden() {
    selectedSearchResult.setValue(null);
  }

  protected void handleSelectedSearchResultChange(SearchResult newValue) {
    if (nonNull(newValue)) {
      if (newValue.isDirectory()) {
        getItems().add(openWithDefaultApplicationMenuItem);
        getItems().add(copyFileToClipbordMenuItem);
      } else {
        getItems().add(openWithDefaultApplicationMenuItem);
        getItems().add(copyFileToClipbordMenuItem);
        getItems().add(openDirectoryContainingFileMenuItem);
      }
    } else {
      getItems().clear();
    }
  }

  protected boolean searchResultIsValid() {
    return nonNull(selectedSearchResult.get()) && nonNull(selectedSearchResult.get().getPath());
  }

  protected void openInOperatingSystem(Path path) {
    fileUtil.openInOperatingSystem(path);
  }

  protected void copyFileToClipboard(Path path) {
    var clipboardContent = new ClipboardContent();
    clipboardContent.putFiles(List.of(path.toFile()));
    Clipboard.getSystemClipboard().setContent(clipboardContent);
  }


  @SuppressWarnings("unused")
  public SearchResult getSelectedSearchResult() {
    return selectedSearchResult.get();
  }

  @SuppressWarnings("unused")
  public ObjectProperty<SearchResult> selectedSearchResultProperty() {
    return selectedSearchResult;
  }

  public void setSelectedSearchResult(SearchResult selectedSearchResult) {
    this.selectedSearchResult.set(selectedSearchResult);
  }
}
