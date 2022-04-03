package com.bytedompteur.documentfinder.persistedqueue.adapter.out;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;

public class FilesReadWriteAdapter {

  public List<String> readAllLines(Path path) throws IOException {
    return Files.readAllLines(path);
  }

  public void deleteIfExists(Path path) throws IOException {
    Files.deleteIfExists(path);
  }

  public BufferedWriter newBufferedWriter(Path of, OpenOption... options) throws IOException {
    return Files.newBufferedWriter(of, options);
  }
}
