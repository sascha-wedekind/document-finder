package com.bytedompteur.documentfinder.directorywatcher.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.Executors;

public class Playground {

  public static void main(String[] args) throws IOException, InterruptedException {
    File directory = new File("X:\\OneDrive\\Dokumente\\DokumentePrivate\\_ERFASSUNGEN");
    Path path = Paths.get(directory.getAbsolutePath());

    DirectoryWatcherImpl directoryWatcher = new DirectoryWatcherImpl(Executors.newFixedThreadPool(1));
    directoryWatcher.watchIncludingSubdirectories(path);

    directoryWatcher.fileEvents().subscribe(e -> {
      System.out.println(e.getType() + " --> " + e.getPath());
    });

    directoryWatcher.startWatching();
    while (true) {
      Thread.sleep(60 * 1000);
    }

  }

}
