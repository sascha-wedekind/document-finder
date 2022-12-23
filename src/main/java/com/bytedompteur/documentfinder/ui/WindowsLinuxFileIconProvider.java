package com.bytedompteur.documentfinder.ui;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;

public class WindowsLinuxFileIconProvider implements SystemFileIconProvider {

  private final FileSystemView FSV = FileSystemView.getFileSystemView();

  @Override
  public Icon getSystemFileIcon(File file) {
    return FSV.getSystemIcon(file);
  }
}
