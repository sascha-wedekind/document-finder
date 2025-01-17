package com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class FilesAdapter {

  public boolean notExists(Path path) {
    return Files.notExists(path);
  }

  public boolean isNotReadable(Path path) {
    return !Files.isReadable(path);
  }

  public boolean isEmpty(Path path) {
    return path.toFile().length() == 0;
  }

  public boolean isOperatingSystemSpecialFile(Path path) {
    var filename = Optional
        .ofNullable(path)
        .map(Path::getFileName)
        .map(Path::toString)
        .orElse("");
    return filename.startsWith("._") ||
        filename.equals("Desktop.ini") ||
        filename.equals("Thumbs.db") ||
        filename.equals("NTUSER.DAT") ||
        filename.equals(".DS_Store") ;
  }
}
