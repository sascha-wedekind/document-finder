package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.ui.FileSystemAdapter;
import javafx.scene.control.MenuItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.testfx.framework.junit5.ApplicationExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class SearchResultTableContextMenuTest {

  private SearchResultTableContextMenu sut;

  @BeforeEach
  void setUp() {
    var mockedFileUtil = Mockito.mock(FileSystemAdapter.class);
    sut = new SearchResultTableContextMenu(mockedFileUtil);
  }

  @Test
  void selectedSearchResultChange_showsNoneDirectoryRelatedMenuItems_whenSearchResultIsFile() {
    // Arrange
    var searchResult = new SearchResult(
      "aFileName",
      "aDirectoryName",
      Instant.now(),
      null,
      false,
      null
    );

    // Act
    sut.setSelectedSearchResult(searchResult);

    // Assert
    assertThat(sut.getItems())
      .extracting(MenuItem::getText)
      .containsExactlyInAnyOrder(
        SearchResultTableContextMenu.OPEN_DIRECTORY_MENU_ITEM_TEXT,
        SearchResultTableContextMenu.COPY_FILE_MENU_ITEM_TEXT,
        SearchResultTableContextMenu.OPEN_WITH_DEFAULT_MENU_ITEM_TEXT
      );
  }

  @Test
  void selectedSearchResultChange_showsDirectoryRelatedMenuItems_whenSearchResultIsDirectory() {
    // Arrange
    var searchResult = new SearchResult(
      "aFileName",
      "aDirectoryName",
      Instant.now(),
      null,
      true,
      null
    );

    // Act
    sut.setSelectedSearchResult(searchResult);

    // Assert
    assertThat(sut.getItems())
      .extracting(MenuItem::getText)
      .containsExactlyInAnyOrder(
        SearchResultTableContextMenu.OPEN_WITH_DEFAULT_MENU_ITEM_TEXT,
        SearchResultTableContextMenu.COPY_FILE_MENU_ITEM_TEXT
      );
  }
}
