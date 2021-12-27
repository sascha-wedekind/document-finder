package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import java.io.Reader;
import java.nio.file.Path;
import java.time.Instant;

public record FileRecord (
  Path path,
  Reader payloadReader,
  Instant created,
  Instant updated
) {

}
