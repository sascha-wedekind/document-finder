package com.bytedompteur.documentfinder.ui.mainwindow;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.FulltextSearchService;
import com.bytedompteur.documentfinder.ui.FxmlFile;
import com.bytedompteur.documentfinder.ui.WindowManager;
import com.bytedompteur.documentfinder.ui.adapter.out.FileSystemAdapter;
import com.bytedompteur.documentfinder.ui.dagger.FxmlLoaderFactory;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testfx.api.FxRobot;
import org.testfx.framework.junit5.ApplicationExtension;
import org.testfx.framework.junit5.Start;

import java.nio.file.Path;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(ApplicationExtension.class)
class MainWindowControllerTest {

    private static final String APPLICATION_HOME_DIR = "/test/home";
    private MainWindowController sut;
    private FileSystemAdapter mockedFileSystemAdapter;

    @Start
    void start(Stage stage) {
        mockedFileSystemAdapter = mock(FileSystemAdapter.class);
        var mockedSearchResultTableController = mock(SearchResultTableController.class);
        var mockedProgressBarController = mock(AnalyzeFilesProgressBarController.class);

        // Avoid NPE in initialize()
        when(mockedSearchResultTableController.getSearchResults()).thenReturn(FXCollections.observableArrayList());

        sut = new MainWindowController(
            mockedSearchResultTableController,
            mockedProgressBarController,
            mock(FulltextSearchService.class),
            mockedFileSystemAdapter,
            mock(WindowManager.class),
            APPLICATION_HOME_DIR
        );

        // We need to provide all controllers for included FXML files
        var fxmlLoaderFactory = new FxmlLoaderFactory(Map.of(
            MainWindowController.class, () -> sut,
            SearchResultTableController.class, () -> mockedSearchResultTableController,
            AnalyzeFilesProgressBarController.class, () -> mockedProgressBarController
        ));

        var node = fxmlLoaderFactory.createParentNode(FxmlFile.MAIN_VIEW);
        stage.setScene(new Scene(node, 800, 600));
        stage.show();
    }

    @Test
    void handleOpenLogDirectoryAction_callsFileSystemAdapterWithHomeDir_whenInvoked(FxRobot robot) {
        // Arrange
        var actionEvent = mock(ActionEvent.class);

        // Act
        sut.handleOpenLogDirectoryAction(actionEvent);

        // Assert
        verify(mockedFileSystemAdapter).openInOperatingSystem(Path.of(APPLICATION_HOME_DIR));
    }
}
