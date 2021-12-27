package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import static java.nio.file.Files.notExists;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Slf4j
public class IndexManager {

  private static IndexManager instance;
  private boolean initialized = false;
  private FSDirectory directory;
  private IndexWriterConfig writerConfig;

  public static IndexManager getInstance() {
    return Optional
      .ofNullable(instance)
      .orElseGet(() -> {
        var manager = new IndexManager();
        manager.init();
        instance = manager;
        return manager;
      });
  }

  public IndexWriter buildIndexWriter() throws IOException {
    return new IndexWriter(directory, writerConfig);
  }

  public IndexReader buildIndexReader() throws IOException {
    return DirectoryReader.open(directory);
  }

  public void init() {
    try {
      Path indexDirectory = determineIndexDirectory();
      ensureIndexDirectoryExists(indexDirectory);
      createLuceneDirectory(indexDirectory);
      createIndexWriterConfig();
      initialized = true;
    } catch (IOException e) {
      log.error("Failed to initialize", e);
    }

  }

  private Path determineIndexDirectory() {
    var indexDirectory = Optional
      .ofNullable(System.getProperty("documentfinder.indexdir"))
      .map(Path::of)
      .orElseGet(() -> {
        var indexDirName = ".documentfinder";
        return Path.of(System.getProperty("user.home"), indexDirName);
      });
    log.info("Determined index directory '{}'", indexDirectory);
    return indexDirectory;
  }

  protected void ensureIndexDirectoryExists(Path indexDirectory) throws IOException {
    if (notExists(indexDirectory)) {
      log.info("Index directory '{}' does not exist. Creating it.", indexDirectory);
      try {
        Files.createDirectories(indexDirectory);
        log.info("Index directory '{}' created", indexDirectory);
      } catch (IOException e) {
        log.error("Could not create index directory '{}'", indexDirectory, e);
        throw e;
      }
    } else {
      log.debug("Index directory '{}' exists", indexDirectory);
    }
  }

  protected void createLuceneDirectory(Path indexDirectory) throws IOException {
    try {
      log.info("Opening Lucene directory '{}'", indexDirectory);
      directory = FSDirectory.open(indexDirectory);
      log.info("Lucene directory '{}' opened", indexDirectory);
    } catch (IOException e) {
      log.error("Could not open Lucene directory '{}'", indexDirectory, e);
      throw e;
    }
  }

  protected void createIndexWriterConfig() throws IOException {
    try {
      log.info("Creating writer analyzer and configuration");
      var analyzer = CustomAnalyzer
        .builder()
        .withTokenizer(WhitespaceTokenizerFactory.NAME)
        .addTokenFilter(LowerCaseFilterFactory.NAME)
        .addTokenFilter(WordDelimiterGraphFilterFactory.NAME, "catenateWords", "1")
        .build();
      writerConfig = new IndexWriterConfig(analyzer);
      log.info("Writer analyzer and configuration created");
    } catch (IOException e) {
      log.error("Could not create writer analyzer and configuration", e);
      throw e;
    }
  }

}
