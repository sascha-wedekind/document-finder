package com.bytedompteur.documentfinder.directorywatcher.adapter.in;

import java.nio.file.Path;
import lombok.Value;

@Value
public class FileWatchEvent {

  public enum Type {CREATE,UPDATE,DELETE}

  Type type;

  Path path;

}
