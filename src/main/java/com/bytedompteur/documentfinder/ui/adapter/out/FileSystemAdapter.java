package com.bytedompteur.documentfinder.ui.adapter.out;

import com.bytedompteur.documentfinder.ui.SystemFileIconProvider;
import javafx.application.HostServices;
import javafx.scene.image.Image;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.Executor;

@RequiredArgsConstructor
@Slf4j
public class FileSystemAdapter {
    private final HostServices hostServices;
    private final SystemFileIconProvider systemFileIconProvider;
    /**
     * Empty when the platform doesn't support {@link Desktop.Action#OPEN}.
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<Desktop> desktop;
    /**
     * Runs the open call off the JavaFX application thread, because {@link Desktop#open} may block.
     */
    private final Executor openExecutor;

    public Optional<Image> getSystemIcon(Path path) {
        Optional<Image> result = Optional.empty();
        try {
            result = Optional.ofNullable(systemFileIconProvider.getSystemFileIcon(path.toFile()));
        } catch (Exception e) {
            log.error("Can't get system icon for '{}'", path, e);
        }
        return result;
    }

    public Optional<Instant> getLastModified(Path path) {
        Optional<Instant> result = Optional.empty();
        try {
            FileTime lastModifiedTime = Files.getLastModifiedTime(path);
            result = Optional.of(lastModifiedTime.toInstant());
        } catch (IOException e) {
            log.error("Can't read last modified timestamp from '{}'", path, e);
        }
        return result;
    }

    /**
     * Opens the file with its associated application, or a directory in the system file manager.
     * <p>
     * Uses {@link Desktop#open} instead of {@link HostServices#showDocument}, because since JavaFX 27
     * {@code showDocument} hands every URI, including {@code file:} URIs, to the default web browser on macOS.
     * {@code HostServices} remains as fallback for platforms without {@code Desktop} support.
     */
    public void openInOperatingSystem(Path path) {
        desktop.ifPresentOrElse(
            it -> openExecutor.execute(() -> openWithDesktop(it, path)),
            () -> hostServices.showDocument(path.toUri().toString())
        );
    }

    private static void openWithDesktop(Desktop desktop, Path path) {
        try {
            desktop.open(path.toFile());
        } catch (Exception e) {
            log.error("Can't open '{}' in operating system", path, e);
        }
    }

}
