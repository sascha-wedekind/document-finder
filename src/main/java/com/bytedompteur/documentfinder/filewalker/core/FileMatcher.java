package com.bytedompteur.documentfinder.filewalker.core;

import java.nio.file.Path;

public interface FileMatcher {

  boolean matches(Path path);

}
