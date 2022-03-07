package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.PathUtil;
import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.UITestInitHelper;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith({MockitoExtension.class, ApplicationExtension.class})
class FolderOptionsControllerTest {

  @Spy
  PathUtil mockedPathUtil;

  private FolderOptionsController sut;
  private Button addToPathsListViewButton;
  private Button removeSelectedPathsButton;

  @Start
  void start(Stage stage) {
    sut = new FolderOptionsController(new OkCancelButtonHandler(), mockedPathUtil);
    UITestInitHelper.addNodeUnderTestToStage(FxmlFile.FOLDER_OPTIONS, sut, stage);

    addToPathsListViewButton = sut.addToPathsListViewButton;
    removeSelectedPathsButton = sut.removeSelectedPathsButton;
  }

  @Test
  void isValidDirectoryToBeAddedToPathList_returnsTrue_whenGivenStringIsAValidAndExistingPath() {
    // Arrange
    Mockito
      .when(mockedPathUtil.isDirectory("/path/to/check"))
      .thenReturn(true);

    // Act
    var result = sut.isValidDirectoryToBeAddedToPathList("/path/to/check");

    // Assert
    assertThat(result).isTrue();
  }

  @Test
  void isValidDirectoryToBeAddedToPathList_returnsFalse_whenGivenStringIsInvalidOrDoesNotExist() {
    // Arrange
    Mockito
      .when(mockedPathUtil.isDirectory("/path/to/check"))
      .thenReturn(false);

    // Act
    var result = sut.isValidDirectoryToBeAddedToPathList("/path/to/check");

    // Assert
    assertThat(result).isFalse();
  }

  @Test
  void pathIsNotChildOfAnyPathInList_returnsFalse_whenGivenPathIsChildOfAnyPathInList() {
    // Arrange
    var paths = List.of(
      "a/b/c",
      "h/i/j"
    );

    // Act
    var result = sut.pathIsNotChildOfAnyPathInList("a/b/c/d", paths);

    // Assert
    assertThat(result).isFalse();
  }

  @Test
  void pathIsNotChildOfAnyPathInList_returnsTrue_whenGivenPathIsNotChildOfAnyPathInList() {
    // Arrange
    var paths = List.of(
      "a/b/c",
      "h/i/j"
    );

    // Act
    var result = sut.pathIsNotChildOfAnyPathInList("x/y", paths);

    // Assert
    assertThat(result).isTrue();
  }

  @Test
  void addToPathListIfNotAlreadyContained(FxRobot robot) {
    robot.interact(() -> {
      // Arrange
      Mockito
        .when(mockedPathUtil.isDirectory(anyString()))
        .thenReturn(true);

      // Act
      sut.addToPathListIfNotAlreadyContained(
        "a/b/c",
        "a/b/c", // shall be ignored because it already exists in list
        "\t ", // shall be ignored because it's empty
        "c/d/e",
        "c/d/e/f" // shall be ignored because it is a child of 'c/d/e'
      );

      // Assert
      assertThat(sut.getPathsList()).containsExactlyInAnyOrder("a/b/c", "c/d/e");
    });
  }

  @Test
  void addToPathListIfNotAlreadyContained_shouldNotThrow_whenGivenParameterIsNull(FxRobot robot) {
    robot.interact(() -> {
      // Act
      sut.addToPathListIfNotAlreadyContained((String) null);

      // Assert
      assertThat(sut.getPathsList()).isEmpty();
    });
  }

  @Test
  void clearPathList_removesAllElementsFromList(FxRobot robot) {
    robot.interact(() -> {
      // Arrange
      Mockito
        .when(mockedPathUtil.isDirectory(anyString()))
        .thenReturn(true);
      sut.addToPathListIfNotAlreadyContained("a", "b", "c");

      // Act
      sut.clearPathsList();

      // Assert
      assertThat(sut.getPathsList()).isEmpty();
    });
  }

  @Test
  void removeByIndicesFromPathsList_removesElementsFromTheList(FxRobot robot) {
    robot.interact(() -> {
      // Arrange
      Mockito
        .when(mockedPathUtil.isDirectory(anyString()))
        .thenReturn(true);
      sut.addToPathListIfNotAlreadyContained("a", "b", "c", "d");

      // Act
      sut.removeByIndicesFromPathsList(List.of(1, 3));

      // Assert
      assertThat(sut.getPathsList()).containsExactlyInAnyOrder("a", "c");
    });
  }

  @Test
  void removeByIndicesFromPathsList_removesNothing_whenParameterIsNull(FxRobot robot) {
    robot.interact(() -> {
      // Arrange
      Mockito
        .when(mockedPathUtil.isDirectory(anyString()))
        .thenReturn(true);
      sut.addToPathListIfNotAlreadyContained("a", "b", "c", "d");

      // Act
      sut.removeByIndicesFromPathsList(null);

      // Assert
      assertThat(sut.getPathsList()).containsExactlyInAnyOrder("a", "b", "c", "d");
    });
  }

  @Test
  void setAddToPathsListViewButtonDisabledStateBasedOnPathsToAddTextFieldSize_disablesButton_whenParameterIsZero() {
    // Act
    sut.setAddToPathsListViewButtonDisabledStateBasedOnPathsToAddTextFieldSize(0);

    // Assert
    assertThat(addToPathsListViewButton)
      .extracting(Node::isDisabled)
      .asInstanceOf(BOOLEAN)
      .isTrue();
  }

  @Test
  void setAddToPathsListViewButtonDisabledStateBasedOnPathsToAddTextFieldSize_enablesButton_whenParameterIsGreaterZero() {
    // Act
    sut.setAddToPathsListViewButtonDisabledStateBasedOnPathsToAddTextFieldSize(3);

    // Assert
    assertThat(addToPathsListViewButton)
      .extracting(Node::isDisabled)
      .asInstanceOf(BOOLEAN)
      .isFalse();
  }

  @Test
  void setRemoveSelectedPathsButtonDisabledStateBasedOnNumberOfSelections_disablesButton_whenNothingSelected() {
    // Act
    sut.setRemoveSelectedPathsButtonDisabledStateBasedOnNumberOfSelections(0);

    // Assert
    assertThat(removeSelectedPathsButton)
      .extracting(Node::isDisabled)
      .asInstanceOf(BOOLEAN)
      .isTrue();
  }

  @Test
  void setRemoveSelectedPathsButtonDisabledStateBasedOnNumberOfSelections_enablesButton_whenItemsSelected() {
    // Act
    sut.setRemoveSelectedPathsButtonDisabledStateBasedOnNumberOfSelections(1);

    // Assert
    assertThat(removeSelectedPathsButton)
      .extracting(Node::isDisabled)
      .asInstanceOf(BOOLEAN)
      .isFalse();
  }
}
