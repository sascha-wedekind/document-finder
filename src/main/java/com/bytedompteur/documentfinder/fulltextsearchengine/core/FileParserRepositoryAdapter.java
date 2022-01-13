package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;

@RequiredArgsConstructor
@Slf4j
public class FileParserRepositoryAdapter implements Runnable {

  private final IndexRepository repository;
  private final FileParserTask parserTask;
  private final Path path;

  @Override
  public void run() {
    try {
      var basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
      var timeCreated = basicFileAttributes.creationTime();
      var timeUpdated = basicFileAttributes.lastModifiedTime();
      var reader = parserTask.getReader();
      repository.save(new FileRecord(path, reader, timeCreated.toInstant(), timeUpdated.toInstant()));
    } catch (IOException e) {
      log.error("", e);
    }
  }
}
