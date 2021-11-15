package com.bytedompteur.documentfinder.persistedqueue.adapter.in;

import java.nio.file.Path;
import lombok.Value;

@Value
public class FileEvent {

  public enum Type {CREATE,UPDATE,DELETE}

  Type type;

  Path path;

}
