package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicLong;

@RequiredArgsConstructor
@Slf4j
public class FileParserRepositoryAdapter implements Runnable {

  private final IndexRepository repository;
  private final FileParserTask parserTask;
  private final Path path;
  private final AtomicLong filesToProcessCountDownReference;

  @Override
  public void run() {
    try {
      parserTask.run();
      var basicFileAttributes = Files.readAttributes(path, BasicFileAttributes.class);
      var timeCreated = basicFileAttributes.creationTime();
      var timeUpdated = basicFileAttributes.lastModifiedTime();
      var reader = parserTask.getReader();
      var locale = parserTask.isLanguageDetectionReliable() ? parserTask.getDetectedLanguage().get() : Locale.ENGLISH;
      log.info("Start indexing '{}'", path);
      repository.save(new FileRecord(path, reader, locale, timeCreated.toInstant(), timeUpdated.toInstant()));
    } catch (Exception e) {
      log.error("Could not index '{}' to repository", path, e);
    } finally {
      log.info("End indexing '{}'", path);
      try {
        parserTask.getReader().close();
      } catch (IOException e) {
        log.error("While closing reader for '{}'", path, e);
      }
      filesToProcessCountDownReference.decrementAndGet();
    }
  }
}
