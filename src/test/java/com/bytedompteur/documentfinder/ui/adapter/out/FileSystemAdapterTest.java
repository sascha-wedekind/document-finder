package com.bytedompteur.documentfinder.ui.adapter.out;

import com.bytedompteur.documentfinder.ui.SystemFileIconProvider;
import javafx.application.HostServices;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class FileSystemAdapterTest {

    private static final Path PATH = Path.of("/a/b/file.pdf");

    @Mock
    HostServices mockedHostServices;

    @Mock
    SystemFileIconProvider mockedSystemFileIconProvider;

    @Mock
    Desktop mockedDesktop;

    @Test
    void openInOperatingSystem_opensFileWithDesktop_whenDesktopIsAvailable() throws IOException {
        // Arrange
        var sut = new FileSystemAdapter(mockedHostServices, mockedSystemFileIconProvider, Optional.of(mockedDesktop), Runnable::run);

        // Act
        sut.openInOperatingSystem(PATH);

        // Assert
        verify(mockedDesktop).open(PATH.toFile());
        verifyNoInteractions(mockedHostServices);
    }

    @Test
    void openInOperatingSystem_fallsBackToHostServices_whenDesktopIsNotAvailable() {
        // Arrange
        var sut = new FileSystemAdapter(mockedHostServices, mockedSystemFileIconProvider, Optional.empty(), Runnable::run);

        // Act
        sut.openInOperatingSystem(PATH);

        // Assert
        verify(mockedHostServices).showDocument(PATH.toUri().toString());
    }

    @Test
    void openInOperatingSystem_doesNotThrow_whenDesktopFailsToOpenFile() throws IOException {
        // Arrange
        doThrow(new IOException("no application associated")).when(mockedDesktop).open(PATH.toFile());
        var sut = new FileSystemAdapter(mockedHostServices, mockedSystemFileIconProvider, Optional.of(mockedDesktop), Runnable::run);

        // Act / Assert
        assertThatCode(() -> sut.openInOperatingSystem(PATH)).doesNotThrowAnyException();
    }
}
