package com.bytedompteur.documentfinder.filewalker.core;

import java.io.IOException;
import java.nio.file.FileVisitor;
import java.nio.file.Path;

public interface WalkFileTreeAdapter {

  Path walkFileTree(Path path, FileVisitor visitor) throws IOException;

}
