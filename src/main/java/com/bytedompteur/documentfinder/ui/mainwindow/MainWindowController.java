package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import com.bytedompteur.documentfinder.settings.adapter.out.PlatformAdapter;
import com.bytedompteur.documentfinder.ui.adapter.out.FileSystemAdapter;
import com.bytedompteur.documentfinder.ui.FxController;
import com.bytedompteur.documentfinder.ui.WindowManager;
import com.bytedompteur.documentfinder.ui.mainwindow.dagger.MainWindowScope;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import lombok.RequiredArgsConstructor;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import jakarta.inject.Inject;

import java.time.Duration;
import java.time.ZoneId;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@MainWindowScope
@SuppressWarnings("java:S1172")
public class MainWindowController implements FxController {

    public static final String SEARCH_RESULTS_TEXT = "search results";
    private final SearchResultTableController searchResultTable;
    private final AnalyzeFilesProgressBarController progressBarController;
    private final FulltextSearchService fulltextSearchService;
    private final FileSystemAdapter fileSystemAdapter;
    private final WindowManager windowManager;
    private final AtomicBoolean ignoreNextSearchAsYouTypeKeyEvent = new AtomicBoolean(false);

    @FXML
    public TextField searchTextField;

    @FXML
    public Label numberOfSearchResultsLabel;

    @FXML
    public Button findLastUpdatedButton;

    private Disposable disposable;

    public void clearView() {
        Platform.runLater(() -> {
            clearSearchResults();
            searchTextField.clear();
        });
    }

    public void showFilesLastUpdated() {
        ignoreNextSearchAsYouTypeKeyEvent.set(true);
        Platform.runLater(() -> {
            findLastUpdatedButton.requestFocus();
            findLastUpdatedButton.fire();
        });
        Platform.runLater(() -> {
            ignoreNextSearchAsYouTypeKeyEvent.set(false);
        });
    }

    public void focusSearchTextField() {
        searchTextField.requestFocus();
    }

    public void searchAsYouType(KeyEvent ignore) {
        if (!ignoreNextSearchAsYouTypeKeyEvent.get()) {
            Platform.runLater(() -> {
                Optional.ofNullable(disposable).ifPresent(Disposable::dispose);
                disposable = Mono
                    .delay(Duration.ofMillis(300))
                    .subscribe(it -> {
                        if (searchTextField.getCharacters().isEmpty()) {
                            clearSearchResults();
                        } else {
                            searchForFilesMatchingSearchText();
                        }
                    });
            });
        }
    }

    @SuppressWarnings("java:S1172")
    public void handleClearSearchTextButtonClick(ActionEvent ignore) {
        clearView();
    }

    public void addToSearchResults(SearchResult it) {
        Platform.runLater(() -> {
            searchResultTable.getSearchResults().add(it);
            numberOfSearchResultsLabel.setText(searchResultTable.getSearchResults().size() + " " + SEARCH_RESULTS_TEXT);
        });
    }

    public void clearSearchResults() {
        Platform.runLater(() -> {
            searchResultTable.getSearchResults().clear();
            numberOfSearchResultsLabel.setText("0 " + SEARCH_RESULTS_TEXT);
        });
    }

    public void searchForFilesMatchingSearchText() {
        Mono
            .just(searchTextField.getCharacters())
            .doOnEach(it -> clearSearchResults())
            .filter(it -> !it.isEmpty())
            .flatMapMany(fulltextSearchService::findFilesWithNamesOrContentMatching)
            .filter(it -> it.getPath().toFile().exists()) // Exclude if file does not exist
            .map(it -> SearchResult.build(
                it.getPath(),
                fileSystemAdapter.getSystemIcon(it.getPath()).map(toImageView()).orElse(null),
                it.getFileLastUpdated().atZone(ZoneId.systemDefault()).toInstant()
            ))
            .subscribe(this::addToSearchResults);
    }

    public void handleFindLastUpdatedButtonClick(ActionEvent ignore) {
        clearView();
        Platform.runLater(() -> {
            fulltextSearchService
                .findLastUpdated()
                .map(it -> SearchResult.build(
                    it.getPath(),
                    fileSystemAdapter.getSystemIcon(it.getPath()).map(toImageView()).orElse(null),
                    it.getFileLastUpdated().atZone(ZoneId.systemDefault()).toInstant()
                ))
                .subscribe(this::addToSearchResults);
        });
    }

    @SuppressWarnings("java:S1172")
    public void handleOptionsButtonClick(ActionEvent ignore) {
        windowManager.showOptionsWindow();
    }

    @FXML
    public void initialize() {
        clearSearchResults();
    }

    @Override
    public void beforeViewHide() {
        progressBarController.beforeViewHide();
    }

    public void handleSearchTextFieldAction(ActionEvent ignore) {
        searchForFilesMatchingSearchText();
    }

    private static Function<Image, ImageView> toImageView() {
        return i -> {
            var result = new ImageView(i);
            result.setSmooth(true);
            result.setFitHeight(28);
            result.setFitWidth(28);
            return result;
        };
    }
}
