package com.bytedompteur.documentfinder.ui.optionswindow;

import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.UITestInitHelper;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.InstanceOfAssertFactories.BOOLEAN;

@ExtendWith(ApplicationExtension.class)
class FileTypeOptionsControllerTest {

  private Button addToFileTypeListViewButton;

  private Button removeSelectedFileTypeButton;

  private FileTypeOptionsController sut;

  @Start
  void start(Stage stage) {
    sut = new FileTypeOptionsController(new OkCancelButtonHandler());
    UITestInitHelper.addNodeUnderTestToStage(FxmlFile.FILE_TYPE_OPTIONS, sut, stage);

    removeSelectedFileTypeButton = sut.removeSelectedFileTypeButton;
    addToFileTypeListViewButton = sut.addToFileTypeListViewButton;
  }

  @Test
  void removeByIndicesFromFileTypeList_removesTypes_whenListIndicesExist(FxRobot robot) {
    robot.interact(() -> {
      // Arrange
      sut.addToFileTypesListIfNotAlreadyContained("pdf", "xls ", "doc", "png");
      var selectedIndices = FXCollections.observableArrayList(1, 3);

      // Act
      sut.removeByIndicesFromFileTypeList(selectedIndices);

      // Assert
      assertThat(sut.getFileTypesList()).containsExactlyInAnyOrder("pdf", "doc");
    });
  }

  @Test
  void removeByIndicesFromFileTypeList_doesNotFail_whenGivenListContainsInvalidIndices(FxRobot robot) {
    robot.interact(() -> {
      // Arrange
      sut.addToFileTypesListIfNotAlreadyContained("pdf");
      var selectedIndices = FXCollections.observableArrayList(1, 2);

      // Act
      sut.removeByIndicesFromFileTypeList(selectedIndices);

      // Assert
      assertThat(sut.getFileTypesList()).containsExactlyInAnyOrder("pdf");
    });
  }

  @Test
  void removeByIndicesFromFileTypeList_doesNotThrow_whenGivenListIsNull() {
    // Act
    var result = catchThrowable(() -> sut.removeByIndicesFromFileTypeList(null));

    // Assert
    assertThat(result).isNull();
  }

  @Test
  void clearFileTypesList_removesAllElementsFromList(FxRobot robot) {
    robot.interact(() -> {
      // Arrange
      sut.addToFileTypesListIfNotAlreadyContained("a", "b", "c");

      // Act
      sut.clearFileTypesList();

      // Assert
      assertThat(sut.getFileTypesList()).isEmpty();
    });
  }

  @Test
  void addToFileTypesListIfNotAlreadyContained_onlyAddsUniqueTypesIgnoringCase(FxRobot robot) {
    robot.interact(() -> {
      // Act
      sut.addToFileTypesListIfNotAlreadyContained("pDF", "pdf", "Pdf", "PDF", "pDf");

      // Assert
      assertThat(sut.getFileTypesList()).containsExactly("pdf");
    });
  }

  @Test
  void addToFileTypesListIfNotAlreadyContained_DoNotTypeIfItAlreadyContainedInList() {
    // Arrange
    sut.addToFileTypesListIfNotAlreadyContained("pdf");

    // Act
    sut.addToFileTypesListIfNotAlreadyContained("pdf");

    // Assert
    assertThat(sut.getFileTypesList()).containsExactly("pdf");
  }

  @Test
  void setAddToFileTypeListViewButtonDisabledStateBasedOnFileTypesToAddTextFieldSize_disablesButton_whenParameterIsZero() {
    // Act
    sut.setAddToFileTypeListViewButtonDisabledStateBasedOnFileTypesToAddTextFieldSize(0);

    // Assert
    assertThat(addToFileTypeListViewButton)
      .extracting(Node::isDisabled)
      .asInstanceOf(BOOLEAN)
      .isTrue();
  }

  @Test
  void setAddToFileTypeListViewButtonDisabledStateBasedOnFileTypesToAddTextFieldSize_enablesButton_whenParameterIsGreaterZero() {
    // Act
    sut.setAddToFileTypeListViewButtonDisabledStateBasedOnFileTypesToAddTextFieldSize(3);

    // Assert
    assertThat(addToFileTypeListViewButton)
      .extracting(Node::isDisabled)
      .asInstanceOf(BOOLEAN)
      .isFalse();
  }

  @Test
  void setRemoveSelectedFileTypeButtonDisabledStateBasedOnNumberOfSelections_disablesButton_whenNothingSelected() {
    // Act
    sut.setRemoveSelectedFileTypeButtonDisabledStateBasedOnNumberOfSelections(0);

    // Assert
    assertThat(removeSelectedFileTypeButton)
      .extracting(Node::isDisabled)
      .asInstanceOf(BOOLEAN)
      .isTrue();
  }

  @Test
  void setRemoveSelectedFileTypeButtonDisabledStateBasedOnNumberOfSelections_enablesButton_whenItemsSelected() {
    // Act
    sut.setRemoveSelectedFileTypeButtonDisabledStateBasedOnNumberOfSelections(1);

    // Assert
    assertThat(removeSelectedFileTypeButton)
      .extracting(Node::isDisabled)
      .asInstanceOf(BOOLEAN)
      .isFalse();
  }
}
