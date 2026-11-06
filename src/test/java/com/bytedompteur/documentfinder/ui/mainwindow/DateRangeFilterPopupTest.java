package com.bytedompteur.documentfinder.ui.mainwindow;

import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ApplicationExtension.class)
class DateRangeFilterPopupTest {

  private DateRangeFilterPopup sut;

  @Start
  void start(Stage stage) {
    sut = new DateRangeFilterPopup();
  }

  @Test
  void isActive_returnsFalse_whenNothingWasSelected() {
    // Assert
    assertThat(sut.isActive()).isFalse();
    assertThat(sut.getSelectedFrom()).isNull();
    assertThat(sut.getSelectedTo()).isNull();
  }

  @Test
  void selectingPreset_appliesTodayAsFromAndTo_whenTodayPresetSelected(FxRobot robot) {
    // Arrange
    var today = LocalDate.now();
    var applied = new boolean[]{false};
    sut.setOnFilterApplied(() -> applied[0] = true);

    // Act
    robot.interact(() -> sut.getPresetComboBox().setValue(DateRangeFilterPopup.DateRangePreset.TODAY));

    // Assert
    assertThat(sut.isActive()).isTrue();
    assertThat(sut.getSelectedFrom()).isEqualTo(today);
    assertThat(sut.getSelectedTo()).isEqualTo(today);
    assertThat(applied[0]).isTrue();
  }

  @Test
  void selectingPreset_appliesLast7Days_whenLast7DaysPresetSelected(FxRobot robot) {
    // Arrange
    var today = LocalDate.now();

    // Act
    robot.interact(() -> sut.getPresetComboBox().setValue(DateRangeFilterPopup.DateRangePreset.LAST_7_DAYS));

    // Assert
    assertThat(sut.getSelectedFrom()).isEqualTo(today.minusDays(6));
    assertThat(sut.getSelectedTo()).isEqualTo(today);
  }

  @Test
  void selectingCustomPreset_revealsDatePickersAndDoesNotApplyYet_whenCustomPresetSelected(FxRobot robot) {
    // Arrange
    var applied = new boolean[]{false};
    sut.setOnFilterApplied(() -> applied[0] = true);

    // Act
    robot.interact(() -> sut.getPresetComboBox().setValue(DateRangeFilterPopup.DateRangePreset.CUSTOM));

    // Assert
    assertThat(sut.getFromDatePicker().isVisible()).isTrue();
    assertThat(sut.getToDatePicker().isVisible()).isTrue();
    assertThat(sut.isActive()).isFalse();
    assertThat(applied[0]).isFalse();
  }

  @Test
  void clickingApply_appliesCustomDates_whenCustomPresetSelectedWithBothDatesSet(FxRobot robot) {
    // Arrange
    var from = LocalDate.of(2024, 6, 1);
    var to = LocalDate.of(2024, 6, 30);
    robot.interact(() -> {
      sut.getPresetComboBox().setValue(DateRangeFilterPopup.DateRangePreset.CUSTOM);
      sut.getFromDatePicker().setValue(from);
      sut.getToDatePicker().setValue(to);
    });

    // Act
    robot.interact(() -> sut.getApplyButton().fire());

    // Assert
    assertThat(sut.isActive()).isTrue();
    assertThat(sut.getSelectedFrom()).isEqualTo(from);
    assertThat(sut.getSelectedTo()).isEqualTo(to);
    assertThat(sut.getActiveFilterDescription()).contains("1 Jun 2024").contains("30 Jun 2024");
  }

  @Test
  void clickingApply_clearsFilter_whenCustomPresetSelectedWithNoDatesSet(FxRobot robot) {
    // Arrange
    robot.interact(() -> sut.getPresetComboBox().setValue(DateRangeFilterPopup.DateRangePreset.CUSTOM));

    // Act
    robot.interact(() -> sut.getApplyButton().fire());

    // Assert
    assertThat(sut.isActive()).isFalse();
    assertThat(sut.getPresetComboBox().getValue()).isEqualTo(DateRangeFilterPopup.DateRangePreset.ANY_TIME);
  }

  @Test
  void clear_resetsToAnyTime_whenAPresetWasApplied(FxRobot robot) {
    // Arrange
    robot.interact(() -> sut.getPresetComboBox().setValue(DateRangeFilterPopup.DateRangePreset.LAST_30_DAYS));
    assertThat(sut.isActive()).isTrue();

    // Act
    robot.interact(() -> sut.clear());

    // Assert
    assertThat(sut.isActive()).isFalse();
    assertThat(sut.getSelectedFrom()).isNull();
    assertThat(sut.getSelectedTo()).isNull();
  }

  @Test
  void clickingClearButton_resetsToAnyTime_whenAPresetWasApplied(FxRobot robot) {
    // Arrange
    robot.interact(() -> sut.getPresetComboBox().setValue(DateRangeFilterPopup.DateRangePreset.THIS_YEAR));

    // Act
    robot.interact(() -> sut.getClearButton().fire());

    // Assert
    assertThat(sut.isActive()).isFalse();
  }
}
