package com.bytedompteur.documentfinder.ui;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@NoArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class FileSystemAdapter {

    private static final FileSystemView FSV = FileSystemView.getFileSystemView();

    public Optional<ImageView> getSystemIcon(Path path) {
        var result = Optional.<ImageView>empty();
        try {
//            var icon = FSV.getSystemIcon(path.toFile(), 265, 265);
            var icon = FSV.getSystemIcon(path.toFile());
            if (Objects.nonNull(icon)) {
                var fxImage = toWritableImage(icon);
                result = Optional.of(new ImageView(fxImage));
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
        try {
            Desktop.getDesktop().open(path.toFile());
        } catch (IOException e) {
            log.error("While OS should open '{}'", path, e);
        }
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
