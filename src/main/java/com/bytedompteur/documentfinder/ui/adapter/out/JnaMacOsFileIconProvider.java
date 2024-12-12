package com.bytedompteur.documentfinder.ui.adapter.out;

import com.bytedompteur.documentfinder.ui.SystemFileIconProvider;
import com.bytedompteur.documentfinder.ui.adapter.out.macos.appkit.NSData;
import com.bytedompteur.documentfinder.ui.adapter.out.macos.appkit.NSWorkspace;
import com.bytedompteur.documentfinder.ui.adapter.out.macos.foundation.NSString;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.WritableImage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <a href="https://github.com/haraldk/TwelveMonkeys-NativeSwing/blob/master/src/main/java/com/twelvemonkeys/spice/osx/OSXImageUtil.java">...</a>
 */
@Slf4j
public class JnaMacOsFileIconProvider implements SystemFileIconProvider {

    /*
     * WARN: UI won't start using Java JNA native MacOS access without having FileChooser initialized.
     */
    @SuppressWarnings("unused")
    private final javax.swing.JFileChooser fc = new javax.swing.JFileChooser();
    private static final JnaMacOsFileIconProvider INSTANCE = new JnaMacOsFileIconProvider();
    private final Map<String, javafx.scene.image.Image> iconsByFileExtensionMap = new ConcurrentHashMap<>();

    private JnaMacOsFileIconProvider() {
    }

    public static JnaMacOsFileIconProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public javafx.scene.image.Image getSystemFileIcon(File file) {
        javafx.scene.image.Image result = null;
        if (file != null && file.isFile()) {
            var extension = FilenameUtils.getExtension(file.getName());
            if (StringUtils.isNoneBlank(extension)) {
                result = iconsByFileExtensionMap.computeIfAbsent(extension, key -> getIcon(file));
            } else {
                result = getIcon(file);
            }
        }
        return result;
    }

    private javafx.scene.image.Image getIcon(File file) {
        try {
            var bytes = getMacOsNativeFileImage(file);
            var bufferedImage = createImage(bytes);
            return scaleImageTo(bufferedImage, 128, 128);
        } catch (Exception e) {
            log.error("Unable to get system file icon for file '{}'", file, e);
            return null;
        }
    }

    private static byte[] getMacOsNativeFileImage(File file) {
        NSString nsString = new NSString(file.getAbsolutePath());
        NSWorkspace NS_WORKSPACE = NSWorkspace.sharedWorkspace();
        var nsImage = NS_WORKSPACE.iconForFile(nsString);
        NSData tiffImageData = nsImage.TIFFRepresentation();
        return tiffImageData.getBytes();
    }

    @SuppressWarnings("SameParameterValue")
    private static WritableImage scaleImageTo(Image bufferedImage, int width, int height) {
        var scaledInstance = bufferedImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return SwingFXUtils.toFXImage(toBufferedImage(scaledInstance), null);
    }

    private static Image createImage(byte[] bytes) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        return ImageIO.read(bais);
    }

    public static BufferedImage toBufferedImage(Image awtImage) {
        if (awtImage instanceof BufferedImage) {
            return (BufferedImage) awtImage;
        }

        BufferedImage bufferedImage = new BufferedImage(
            awtImage.getWidth(null),
            awtImage.getHeight(null),
            BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(awtImage, 0, 0, null);
        g2d.dispose();

        return bufferedImage;
    }
}
