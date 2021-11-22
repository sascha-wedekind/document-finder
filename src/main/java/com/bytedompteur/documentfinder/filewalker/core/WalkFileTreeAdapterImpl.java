package com.bytedompteur.documentfinder.filewalker.core;

import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;

public class WalkFileTreeAdapterImpl implements
  WalkFileTreeAdapter {

  @Override
  public Path walkFileTree(Path path, FileVisitor visitor) throws IOException {
    return Files.walkFileTree(path, visitor);
  }
}
