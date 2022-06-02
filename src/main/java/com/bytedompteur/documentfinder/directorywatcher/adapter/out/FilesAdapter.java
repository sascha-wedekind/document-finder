package com.bytedompteur.documentfinder.directorywatcher.adapter.out;

import java.io.IOException;
import java.nio.file.*;

public class FilesAdapter {

  public void walkFileTree(Path start, FileVisitor<? super Path> visitor) throws IOException {
    Files.walkFileTree(start, visitor);
  }

  public WatchService createWatchService() throws IOException {
    return FileSystems.getDefault().newWatchService();
  }

}
