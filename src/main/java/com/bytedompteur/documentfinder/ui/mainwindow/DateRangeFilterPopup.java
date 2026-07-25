package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowScope;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import jakarta.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@MainWindowScope
public class DateRangeFilterPopup extends ContextMenu {

    public enum DateRangePreset {
        ANY_TIME("Any time"),
        TODAY("Today"),
        LAST_7_DAYS("Last 7 days"),
        LAST_30_DAYS("Last 30 days"),
        THIS_YEAR("This year"),
        CUSTOM("Custom range…");

        private final String label;

        DateRangePreset(String label) {
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final DateTimeFormatter CHIP_DATE_FORMATTER = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.US);

    private final ComboBox<DateRangePreset> presetComboBox = new ComboBox<>();
    private final DatePicker fromDatePicker = new DatePicker();
    private final DatePicker toDatePicker = new DatePicker();
    private final Button applyButton = new Button("Apply");
    private final Button clearButton = new Button("Clear");

    private DateRangePreset appliedPreset = DateRangePreset.ANY_TIME;
    private LocalDate appliedFrom;
    private LocalDate appliedTo;
    private Runnable onFilterApplied = () -> {
    };

    @Inject
    public DateRangeFilterPopup() {
        presetComboBox.getItems().setAll(DateRangePreset.values());
        presetComboBox.setValue(DateRangePreset.ANY_TIME);
        presetComboBox.setMaxWidth(Double.MAX_VALUE);
        presetComboBox.valueProperty().addListener((observable, oldValue, newValue) -> handlePresetSelected());

        fromDatePicker.setPromptText("From");
        toDatePicker.setPromptText("To");
        fromDatePicker.setVisible(false);
        fromDatePicker.setManaged(false);
        toDatePicker.setVisible(false);
        toDatePicker.setManaged(false);

        applyButton.setOnAction(event -> handleApply());
        clearButton.setOnAction(event -> handleClear());
        ButtonBar.setButtonData(clearButton, ButtonBar.ButtonData.LEFT);
        ButtonBar.setButtonData(applyButton, ButtonBar.ButtonData.OK_DONE);
        var buttonBar = new ButtonBar();
        buttonBar.getButtons().addAll(clearButton, applyButton);

        var customDatesBox = new HBox(5.0, fromDatePicker, toDatePicker);

        var content = new VBox(10.0, presetComboBox, customDatesBox, buttonBar);
        content.setPadding(new Insets(10.0));
        content.setPrefWidth(260.0);

        var menuItem = new CustomMenuItem(content);
        menuItem.setHideOnClick(false);
        getItems().add(menuItem);
    }

    public void setOnFilterApplied(Runnable onFilterApplied) {
        this.onFilterApplied = onFilterApplied == null ? () -> {
        } : onFilterApplied;
    }

    public LocalDate getSelectedFrom() {
        return appliedFrom;
    }

    public LocalDate getSelectedTo() {
        return appliedTo;
    }

    public boolean isActive() {
        return appliedFrom != null || appliedTo != null;
    }

    public String getActiveFilterDescription() {
        if (appliedPreset != DateRangePreset.CUSTOM) {
            return appliedPreset.toString();
        }
        if (appliedFrom != null && appliedTo != null) {
            return CHIP_DATE_FORMATTER.format(appliedFrom) + " – " + CHIP_DATE_FORMATTER.format(appliedTo);
        }
        if (appliedFrom != null) {
            return "From " + CHIP_DATE_FORMATTER.format(appliedFrom);
        }
        if (appliedTo != null) {
            return "Until " + CHIP_DATE_FORMATTER.format(appliedTo);
        }
        return DateRangePreset.ANY_TIME.toString();
    }

    public void clear() {
        fromDatePicker.setValue(null);
        toDatePicker.setValue(null);
        presetComboBox.setValue(DateRangePreset.ANY_TIME);
    }

    // For testing

    protected ComboBox<DateRangePreset> getPresetComboBox() {
        return presetComboBox;
    }

    protected DatePicker getFromDatePicker() {
        return fromDatePicker;
    }

    protected DatePicker getToDatePicker() {
        return toDatePicker;
    }

    protected Button getApplyButton() {
        return applyButton;
    }

    protected Button getClearButton() {
        return clearButton;
    }

    private void handlePresetSelected() {
        var preset = presetComboBox.getValue();
        var isCustom = preset == DateRangePreset.CUSTOM;
        fromDatePicker.setVisible(isCustom);
        fromDatePicker.setManaged(isCustom);
        toDatePicker.setVisible(isCustom);
        toDatePicker.setManaged(isCustom);

        if (!isCustom) {
            var range = toRange(preset);
            applyPreset(preset, range[0], range[1]);
            hide();
        }
    }

    private void handleApply() {
        var from = fromDatePicker.getValue();
        var to = toDatePicker.getValue();
        if (from == null && to == null) {
            clear();
            return;
        }
        applyPreset(DateRangePreset.CUSTOM, from, to);
        hide();
    }

    private void handleClear() {
        clear();
    }

    private void applyPreset(DateRangePreset preset, LocalDate from, LocalDate to) {
        this.appliedPreset = preset;
        this.appliedFrom = from;
        this.appliedTo = to;
        onFilterApplied.run();
    }

    private static LocalDate[] toRange(DateRangePreset preset) {
        var today = LocalDate.now();
        return switch (preset) {
            case TODAY -> new LocalDate[]{today, today};
            case LAST_7_DAYS -> new LocalDate[]{today.minusDays(6), today};
            case LAST_30_DAYS -> new LocalDate[]{today.minusDays(29), today};
            case THIS_YEAR -> new LocalDate[]{LocalDate.of(today.getYear(), 1, 1), today};
            case ANY_TIME, CUSTOM -> new LocalDate[]{null, null};
        };
    }
}
