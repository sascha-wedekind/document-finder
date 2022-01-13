package com.bytedompteur.documentfinder.persistedqueue.adapter.in;

import lombok.Value;

import java.nio.file.Path;

@Value
public class FileEvent {

  public enum Type {CREATE,UPDATE,DELETE,UNKNOWN}

  Type type;

  Path path;

}
