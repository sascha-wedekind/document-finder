package com.bytedompteur.documentfinder.fulltextsearchengine.adapter.out;

import java.nio.file.Path;
import lombok.Value;

@Value
public class FileEvent {

  public enum Type {DELETE,OTHER}

  Type type;

  Path path;

}
