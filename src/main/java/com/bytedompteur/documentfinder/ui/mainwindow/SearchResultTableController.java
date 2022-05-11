package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.ui.FileSystemAdapter;
import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowScope;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;
import lombok.RequiredArgsConstructor;

import javax.inject.Inject;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@MainWindowScope
public class SearchResultTableController implements FxController {

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
    // Autolayout columns on table resize. 40%, 20%, 20%
    resultTable.widthProperty().addListener(new ChangeListener<Number>() {
      @Override
      public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        resultTable.getColumns().get(1).setMaxWidth(1f * Integer.MAX_VALUE * 40); // Filename
        resultTable.getColumns().get(2).setMaxWidth(1f * Integer.MAX_VALUE * 20); // File path
        resultTable.getColumns().get(3).setMaxWidth(1f * Integer.MAX_VALUE * 20); // File modification date
      }
    });


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
      .filter(it -> it.getId().equals("pathParentColumn"))
      .findFirst()
      .map(it -> (TableColumn<SearchResult, String>) it)
      .ifPresent(this::applyPathParentColumnCellFactory);

    resultTable.setContextMenu(contextMenu);
    resultTable.setOnContextMenuRequested(event -> Optional
      .ofNullable(resultTable.getSelectionModel().getSelectedItem())
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

  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

  private void applyFileModifiedColumnCellFactory(TableColumn<SearchResult, Instant> column) {
    column.setCellFactory(new Callback<>() {
      @Override
      public TableCell<SearchResult, Instant> call(TableColumn<SearchResult, Instant> param) {
        return new TableCell<>() {
          @Override
          protected void updateItem(Instant item, boolean empty) {
            if (Objects.equals(item, getItem())) {
              return;
            }
            super.updateItem(item, empty);
            if (Objects.nonNull(item)) {
              super.setText(DATE_FORMATTER.format(item.atZone(ZoneId.systemDefault())));
              super.setGraphic(null);
            } else {
              super.setText(null);
              super.setGraphic(null);
            }
          }
        };
      }
    });
  }

  private void applyPathParentColumnCellFactory(TableColumn<SearchResult, String> column) {
    column.setCellFactory(param -> {
      return new TableCell<>() {
        @Override
        protected void updateItem(String item, boolean empty) {
          super.updateItem(item, empty);
          if (empty) {
            super.setText(null);
            super.setGraphic(null);
          } else {
            var label = new Label();
            label.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
            label.setText(item);
            label.setTooltip(new Tooltip(item));
            super.setGraphic(label);
          }
        }
      };
    });
  }

}
