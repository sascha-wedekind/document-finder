package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.ui.adapter.out.FileSystemAdapter;
import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowScope;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import lombok.RequiredArgsConstructor;

import jakarta.inject.Inject;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@MainWindowScope
public class SearchResultTableController implements FxController {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");


    private final SearchResultTableContextMenu contextMenu;

    private final ObservableList<SearchResult> searchResults = FXCollections.observableArrayList();

    private final FileSystemAdapter fileUtil;

    @FXML
    public TableView<SearchResult> resultTable;

    public ObservableList<SearchResult> getSearchResults() {
        return searchResults;
    }

    @FXML
    public void initialize() {
        var format = DATE_FORMATTER.format(LocalDateTime.now());
        var text = new Text();
        text.setText(format);

        resultTable.widthProperty().addListener((observable, oldValue, newValue) -> {
            resultTable.getColumns().get(1).setMaxWidth(1f * Integer.MAX_VALUE * 60); // Filename
            resultTable.getColumns().get(2).setMaxWidth(text.getLayoutBounds().getWidth() + 40);
            resultTable.getColumns().get(2).setMinWidth(text.getLayoutBounds().getWidth() + 40);
        });

        resultTable.getSelectionModel().setSelectionMode(javafx.scene.control.SelectionMode.MULTIPLE);

        //noinspection unchecked
        resultTable.getColumns()
            .stream()
            .filter(it -> it.getId().equals("fileModifiedColumn"))
            .findFirst()
            .map(it -> (TableColumn<SearchResult, Instant>) it)
            .ifPresent(this::applyFileModifiedColumnCellFactory);

        //noinspection unchecked
        resultTable.getColumns()
            .stream()
            .filter(it -> it.getId().equals("pathNameColumn"))
            .findFirst()
            .map(it -> (TableColumn<SearchResult, SearchResult>) it)
            .ifPresent(column -> {
                column.cellValueFactoryProperty().set(param -> new ReadOnlyObjectWrapper<>(param.getValue()));
                column.cellFactoryProperty().set(param -> new TwoLineTableCell());
            });

        resultTable.setContextMenu(contextMenu);
        resultTable.setOnContextMenuRequested(event -> Optional
            .ofNullable(resultTable.getSelectionModel().getSelectedItems())
            .ifPresent(it -> {
                contextMenu.setSelectedSearchResult(it);
                contextMenu.show(event.getPickResult().getIntersectedNode(), event.getScreenX(), event.getScreenY());
            }));
        resultTable.setRowFactory(param -> { // Row factory to register double click listener
            var result = new TableRow<SearchResult>();
            result.setOnMouseClicked(handleTableRowLeftClick(result));
            return result;
        });
    }

    protected EventHandler<MouseEvent> handleTableRowLeftClick(TableRow<SearchResult> result) {
        return e -> {
            if (e.getClickCount() == 2) {
                fileUtil.openInOperatingSystem(result.getItem().getPath());
            }
        };
    }

    private void applyFileModifiedColumnCellFactory(TableColumn<SearchResult, Instant> column) {
        column.setCellFactory(it -> new TableCell<>() {
            @Override
            protected void updateItem(Instant item, boolean empty) {
                if (Objects.equals(item, getItem())) {
                    return;
                }
                super.updateItem(item, empty);
                super.getStyleClass().add("file-modified-cell");
                if (Objects.nonNull(item)) {
                    super.setText(DATE_FORMATTER.format(item.atZone(ZoneId.systemDefault())));
                    super.setGraphic(null);
                } else {
                    super.setText(null);
                    super.setGraphic(null);
                }
            }
        });
    }
}
