package com.bytedompteur.documentfinder.ui.mainwindow;

import javafx.scene.image.ImageView;
import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

@Value
@AllArgsConstructor
@Slf4j
public class SearchResult {

  String fileOrDirectoryName;
  String directoryName;
  Instant updated;
  ImageView icon;
  boolean directory;
  Path path;

  public static SearchResult build(Path path, ImageView icon, Instant lastModified) {
    var isDirectory = Files.isDirectory(path);
    return new SearchResult(
            path.getFileName().toString(),
            isDirectory ? "---" : path.getParent().toString(),
            lastModified,
            icon,
            isDirectory,
            path
    );
  }

}
