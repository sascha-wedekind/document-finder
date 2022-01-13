package com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out;

import lombok.Value;

import java.nio.file.Path;

@Value
public class FileEvent {

  public enum Type {DELETE,OTHER}

  Type type;

  Path path;

}
