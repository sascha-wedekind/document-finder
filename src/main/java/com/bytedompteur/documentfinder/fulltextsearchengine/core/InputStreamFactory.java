package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import java.io.*;
import java.nio.file.Path;

public class InputStreamFactory {

    public InputStream create(Path path) throws IOException {
        return new BufferedInputStream(new FileInputStream(path.toFile()));
    }

}
