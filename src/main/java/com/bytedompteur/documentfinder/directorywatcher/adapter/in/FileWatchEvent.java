package com.bytedompteur.documentfinder.directorywatcher.adapter.in;

import lombok.Value;

import java.nio.file.Path;

@Value
public class FileWatchEvent {

  public enum Type {CREATE,UPDATE,DELETE}

  Type type;

  Path path;

}
