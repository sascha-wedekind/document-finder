package com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out;

import java.nio.file.Files;
import java.nio.file.Path;

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
}
