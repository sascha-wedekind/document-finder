package com.bytedompteur.documentfinder.directorywatcher.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Playground {

  public static void main(String[] args) throws IOException, InterruptedException {
    File directory = new File("X:\\OneDrive\\Dokumente\\DokumentePrivate\\_ERFASSUNGEN");
    Path path = Paths.get(directory.getAbsolutePath());

    DirectoryWatcher directoryWatcher = new DirectoryWatcher();
    directoryWatcher.watchIncludingSubdirectories(path);

    directoryWatcher.fileEvents().subscribe(e -> {
      System.out.println(e.kind() + " --> " + e.context());
    });

    directoryWatcher.startWatching();
    while (true) {
      Thread.sleep(60 * 1000);
    }

  }

}
