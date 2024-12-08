package com.bytedompteur.documentfinder.ui.adapter.out;

import com.bytedompteur.documentfinder.ui.SystemFileIconProvider;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

import javax.swing.filechooser.FileSystemView;
import java.awt.image.BufferedImage;
import java.io.File;

public class WindowsLinuxFileIconProvider implements SystemFileIconProvider {

  private final FileSystemView FSV = FileSystemView.getFileSystemView();

  @Override
  public Image getSystemFileIcon(File file) {
    var icon = FSV.getSystemIcon(file);
    BufferedImage bufferedImage = new BufferedImage(
        icon.getIconWidth(),
        icon.getIconHeight(),
        BufferedImage.TYPE_INT_ARGB
    );
    icon.paintIcon(null, bufferedImage.getGraphics(), 0, 0);
    return SwingFXUtils.toFXImage(bufferedImage, null);

//    return icon;
  }
}
