package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.ui.adapter.out.FileSystemAdapter;
import com.bytedompteur.documentfinder.ui.adapter.out.JavaFxPlatformAdapter;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class SearchResultTableContextMenuTest {

  private SearchResultTableContextMenu sut;
  private JavaFxPlatformAdapter mockedPlatformAdapter;
  private FileSystemAdapter mockedFileSystemAdapter;
  private TextField textField;

  @Start
  public void start(Stage stage) {
    mockedPlatformAdapter = mock(JavaFxPlatformAdapter.class);
    mockedFileSystemAdapter = mock(FileSystemAdapter.class);
    sut = new SearchResultTableContextMenu(mockedFileSystemAdapter, mockedPlatformAdapter);
    sut.setId("searchResultTableContextMenu");

    textField = new TextField();
    textField.setContextMenu(sut);
    VBox vbox = new VBox(textField);
    stage.setScene(new Scene(vbox, 300, 200));
    stage.setAlwaysOnTop(true);
    stage.show();
  }

  @Test
  void prepareContextMenuItems_setsFileAndDirectoryRelatedMenuItemTextsInSingular_whenSearchResultContains1FileAnd1Directory() {
    // Arrange
    var searchResult = buildSearchResult(1, 1);

    // Act
    sut.setSelectedSearchResult(searchResult);
    sut.prepareContextMenuItems();

    // Assert
    var menuItemTexts = sut.getItems().stream().map(MenuItem::getText).toList();

    assertThat(menuItemTexts)
      .containsExactlyInAnyOrder(
        "Copy file to clipboard",
        "Open file with default application",
        "Open directory containing file",
        "Copy directory to clipboard",
        "Open directory"
      );
  }

  @Test
  void prepareContextMenuItems_setsFileAndDirectoryRelatedMenuItemTextsInPlural_whenSearchResultContainsAtLeast2FilesAnd2Directories() {
    // Arrange
    var searchResult = buildSearchResult(2, 2);

    // Act
    sut.setSelectedSearchResult(searchResult);
    sut.prepareContextMenuItems();

    // Assert
    var menuItemTexts = sut.getItems().stream().map(MenuItem::getText).toList();

    assertThat(menuItemTexts)
      .containsExactlyInAnyOrder(
        "Copy 2 files to clipboard",
        "Open 2 files with their default applications",
        "Open directories containing 2 files",
        "Copy 2 directories to clipboard",
        "Open 2 directories"
      );
  }

  @ParameterizedTest
  @MethodSource("namedArguments")
  @SuppressWarnings("java:S2699")
  void contextMenuEntry(ArgumentsAccessor arguments, FxRobot robot) {
    robot
      // Arrange
      .interact(() -> {
        sut.setSelectedSearchResult(buildSearchResult(arguments.getInteger(0),arguments.getInteger(1)));
        sut.prepareContextMenuItems();
      })

      // Act
      .interact(() -> {
        // Show context menu without using the mouse, because the right click with FxRobot doesn't work in headless
        // tests (JavaFx Monocle renderer)
        Bounds boundsInParent = textField.getBoundsInParent();
        Point2D topLeft = textField.localToScreen(boundsInParent.getMinX(), boundsInParent.getMinY());
        var contextMenu = textField.getContextMenu();
        contextMenu.show(textField, topLeft.getX(), topLeft.getY());
      })
      .interact(() -> {
        // Click on context menu entry having the given text
        // Implemented this way because search with FxRobot doesn't work in headless tests (JavaFx Monocle renderer)
        var contextMenu = textField.getContextMenu();
        var menuItem = contextMenu.getItems().stream()
          .filter(item -> item.getText().equals(arguments.getString(2)))
          .findFirst()
          .orElseThrow();
        menuItem.fire();
      })
      .interact(() -> {
        // Hide context menu
        textField.getContextMenu().hide();
      })

      // Assert
      .interact(() -> {
        //noinspection unchecked
        arguments.get(3, Consumer.class).accept(this);
      });
  }


  static List<SearchResult> buildSearchResult(int numberOfFiles, int numberOfDirectories) {
    var files = IntStream.range(0, numberOfFiles)
      .mapToObj(i -> new SearchResult(
        "aFileName_" + i,
        "aParentDirectoryName_" + (i % 2),
        Instant.now(),
        null,
        false,
        Path.of("aParentDirectoryName_" + (i % 2), "aFileName_" + i)
      ));

    var directories = IntStream.range(0, numberOfDirectories)
      .mapToObj(i -> new SearchResult(
        "aDirectoryName_" + i,
        "aParentDirectoryName",
        Instant.now(),
        null,
        true,
        Path.of("aParentDirectoryName", "aDirectoryName_" + i)
      ));

    return Stream.concat(files, directories).toList();
  }

  /*
   * ===
   */

  public static Stream<Arguments> namedArguments() {
    return Stream.of(
      Arguments.of( 1,1,"Copy file to clipboard", assertCopyFileToClipboardResult()),
      Arguments.of( 2,2,"Copy 2 files to clipboard", assertCopyFilesToClipboardResult()),
      Arguments.of( 1,1,"Open directory containing file", assertOpenDirectoryContainingFile()),
      Arguments.of( 3,1,"Open directories containing 3 files", assertOpenDirectoriesContainingFiles()),
      Arguments.of( 1,1,"Open file with default application", assertOpenFileWithDefaultApplication()),
      Arguments.of( 2,1,"Open 2 files with their default applications", assertOpenFilesWithDefaultApplications()),
      Arguments.of( 1,1,"Copy directory to clipboard", assertCopyDirectoryToClipboard()),
      Arguments.of( 1,2,"Copy 2 directories to clipboard", assertCopyDirectoriesToClipboard())
    );
  }

  static Consumer<SearchResultTableContextMenuTest> assertCopyFileToClipboardResult() {
    return testInstance -> {
      ArgumentCaptor<ClipboardContent> argumentCaptor = ArgumentCaptor.forClass(ClipboardContent.class);
      verify(testInstance.mockedPlatformAdapter).setClipboardContent(argumentCaptor.capture());
      var clipboardContent = argumentCaptor.getValue();
      assertThat(clipboardContent.getFiles()).containsExactly(
        Path.of("aParentDirectoryName_0", "aFileName_0").toFile()
      );
    };
  }

  static Consumer<SearchResultTableContextMenuTest> assertCopyFilesToClipboardResult() {
    return testInstance -> {
      ArgumentCaptor<ClipboardContent> argumentCaptor = ArgumentCaptor.forClass(ClipboardContent.class);
      verify(testInstance.mockedPlatformAdapter).setClipboardContent(argumentCaptor.capture());
      var clipboardContent = argumentCaptor.getValue();
      assertThat(clipboardContent.getFiles()).containsExactly(
        Path.of("aParentDirectoryName_0", "aFileName_0").toFile(),
        Path.of("aParentDirectoryName_1", "aFileName_1").toFile()
      );
    };
  }

  static Consumer<SearchResultTableContextMenuTest> assertOpenDirectoryContainingFile() {
    return testInstance -> {
      ArgumentCaptor<Path> argumentCaptor = ArgumentCaptor.forClass(Path.class);
      verify(testInstance.mockedFileSystemAdapter).openInOperatingSystem(argumentCaptor.capture());
      var path = argumentCaptor.getValue();
      assertThat(path).isEqualTo(Path.of("aParentDirectoryName_0"));
    };
  }

  static Consumer<SearchResultTableContextMenuTest> assertOpenDirectoriesContainingFiles() {
    return testInstance -> {
      ArgumentCaptor<Path> argumentCaptor = ArgumentCaptor.forClass(Path.class);
      verify(testInstance.mockedFileSystemAdapter, atLeastOnce()).openInOperatingSystem(argumentCaptor.capture());
      var paths = argumentCaptor.getAllValues();
      // Director name suffix is array index modulus 2, so there should be 2 unique directory names
      assertThat(paths).containsExactly(Path.of("aParentDirectoryName_0"), Path.of("aParentDirectoryName_1"));
    };

  }

  private static Consumer<SearchResultTableContextMenuTest> assertOpenFileWithDefaultApplication() {
    return testInstance -> {
      ArgumentCaptor<Path> argumentCaptor = ArgumentCaptor.forClass(Path.class);
      verify(testInstance.mockedFileSystemAdapter).openInOperatingSystem(argumentCaptor.capture());
      var paths = argumentCaptor.getValue();
      // Director name suffix is array index modulus 2, so there should be 2 unique directory names
      assertThat(paths).isEqualTo(Path.of("aParentDirectoryName_0", "aFileName_0"));
    };
  }

  private static Consumer<SearchResultTableContextMenuTest> assertOpenFilesWithDefaultApplications() {
    return testInstance -> {
      ArgumentCaptor<Path> argumentCaptor = ArgumentCaptor.forClass(Path.class);
      verify(testInstance.mockedFileSystemAdapter, atLeastOnce()).openInOperatingSystem(argumentCaptor.capture());
      var paths = argumentCaptor.getAllValues();
      // Director name suffix is array index modulus 2, so there should be 2 unique directory names
      assertThat(paths).containsExactly(Path.of("aParentDirectoryName_0", "aFileName_0"), Path.of("aParentDirectoryName_1", "aFileName_1"));
    };
  }

  private static Consumer<SearchResultTableContextMenuTest> assertCopyDirectoryToClipboard() {
    return testInstance -> {
      ArgumentCaptor<ClipboardContent> argumentCaptor = ArgumentCaptor.forClass(ClipboardContent.class);
      verify(testInstance.mockedPlatformAdapter).setClipboardContent(argumentCaptor.capture());
      var clipboardContent = argumentCaptor.getValue();
      assertThat(clipboardContent.getFiles()).containsExactly(
        Path.of("aParentDirectoryName", "aDirectoryName_0").toFile()
      );
    };
  }

  private static Consumer<SearchResultTableContextMenuTest> assertCopyDirectoriesToClipboard() {
    return testInstance -> {
      ArgumentCaptor<ClipboardContent> argumentCaptor = ArgumentCaptor.forClass(ClipboardContent.class);
      verify(testInstance.mockedPlatformAdapter).setClipboardContent(argumentCaptor.capture());
      var clipboardContent = argumentCaptor.getValue();
      assertThat(clipboardContent.getFiles()).containsExactly(
        Path.of("aParentDirectoryName", "aDirectoryName_0").toFile(),
        Path.of("aParentDirectoryName", "aDirectoryName_1").toFile()
      );
    };
  }
}
