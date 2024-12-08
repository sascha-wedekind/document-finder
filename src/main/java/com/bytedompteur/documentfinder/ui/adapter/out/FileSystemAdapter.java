package com.bytedompteur.documentfinder.ui.adapter.out;

import com.bytedompteur.documentfinder.ui.SystemFileIconProvider;
import jakarta.inject.Inject;
import javafx.application.HostServices;
import javafx.scene.image.Image;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Optional;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class FileSystemAdapter {
    private final HostServices hostServices;
    private final SystemFileIconProvider systemFileIconProvider;

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

    public void openInOperatingSystem(Path path) {
        hostServices.showDocument(path.toUri().toString());
    }

}
