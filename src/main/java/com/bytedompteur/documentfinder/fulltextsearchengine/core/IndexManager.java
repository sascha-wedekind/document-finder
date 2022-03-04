package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.Files.notExists;

@RequiredArgsConstructor
@Getter
@Slf4j
public class IndexManager {

  private IndexManager instance;
  private boolean initialized = false;
  private FSDirectory directory;
  private IndexWriterConfig writerConfig;
  private final String applicationHomeDir;

  public IndexWriter buildIndexWriter() throws IOException {
    return new IndexWriter(directory, writerConfig);
  }

  public IndexReader buildIndexReader(IndexWriter value) throws IOException {
//    return DirectoryReader.open(directory);
    return DirectoryReader.open(value);
  }

  public void init() {
    try {
      var indexDirectory = Paths.get(applicationHomeDir, "index");
      ensureIndexDirectoryExists(indexDirectory);
      createLuceneDirectory(indexDirectory);
      createIndexWriterConfig();
      initialized = true;
    } catch (IOException e) {
      log.error("Failed to initialize", e);
    }

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
