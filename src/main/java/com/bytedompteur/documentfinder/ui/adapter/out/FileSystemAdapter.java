package com.bytedompteur.documentfinder.ui.adapter.out;

import com.bytedompteur.documentfinder.ui.SystemFileIconProvider;
import javafx.application.HostServices;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Optional;

import static java.util.Objects.nonNull;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class FileSystemAdapter {
    private final HostServices hostServices;
    private final SystemFileIconProvider systemFileIconProvider;

    public Optional<WritableImage> getSystemIcon(Path path) {
        var result = Optional.<WritableImage>empty();
        try {
            var icon = systemFileIconProvider.getSystemFileIcon(path.toFile());
            if (nonNull(icon)) {
                result = Optional.of(toWritableImage(icon));
            }
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

    private static WritableImage toWritableImage(Icon swingIcon) {
        BufferedImage bufferedImage = new BufferedImage(
                swingIcon.getIconWidth(),
                swingIcon.getIconHeight(),
                BufferedImage.TYPE_INT_ARGB
        );
        swingIcon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

}
