package com.bytedompteur.documentfinder.settings.core;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class FilesReadWriteAdapter {

  public Path writeString(Path path, CharSequence text) throws IOException {
    return Files.writeString(path, text, StandardCharsets.UTF_8);
  }

  public String readString(Path path) throws IOException {
    return Files.readString(path, StandardCharsets.UTF_8);
  }
}
