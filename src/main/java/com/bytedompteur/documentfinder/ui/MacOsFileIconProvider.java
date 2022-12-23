package com.bytedompteur.documentfinder.ui;

import javax.swing.*;
import java.io.File;

public class MacOsFileIconProvider implements SystemFileIconProvider {

  private final javax.swing.JFileChooser fc = new javax.swing.JFileChooser();

  @Override
  public Icon getSystemFileIcon(File file) {
    return fc.getUI().getFileView(fc).getIcon(file);
  }
}
