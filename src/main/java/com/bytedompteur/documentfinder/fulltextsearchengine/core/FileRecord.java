package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import java.io.Reader;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Locale;

public record FileRecord (
    Path path,
    Reader payloadReader,
    Locale locale,
    Instant created,
    Instant updated
) {

}
