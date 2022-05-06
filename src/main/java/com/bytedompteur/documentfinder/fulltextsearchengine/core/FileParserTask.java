package com.bytedompteur.documentfinder.fulltextsearchengine.core;


import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class FileParserTask implements Runnable {

  public enum State{UNUSED,RUNNING, FINISHED}

  private final Path path;
  private final PipedWriter pipedWriter;
  private final PipedReader pipedReader;
  private final AutoDetectParser autoDetectParser = new AutoDetectParser();
  private final Metadata metadata = new Metadata();
  private final AtomicReference<Exception> exceptionWhileParsing = new AtomicReference<>(null);
  private final AtomicReference<State> state = new AtomicReference<>(State.UNUSED);

  private FileParserTask(Path path, PipedWriter pipedWriter, PipedReader pipedReader) {
    this.path = path;
    this.pipedWriter = pipedWriter;
    this.pipedReader = pipedReader;
  }

  @SuppressWarnings("java:S2095")
  public static FileParserTask create(Path path) throws IOException {
    var pipedReader = new PipedReader(250 * 1024);
    var pipedWriter = new PipedWriter(pipedReader);
    return new FileParserTask(path, pipedWriter, pipedReader);
  }

  public Reader getReader() {
    return pipedReader;
  }

  public State getState() {
    return state.get();
  }

  public Optional<Exception> getExceptionThrownWhileParsing() {
    return Optional.ofNullable(exceptionWhileParsing.get());
  }

  @Override
  public void run() {
    state.set(State.RUNNING);
    try (var fis = new FileInputStream(path.toFile())) {
      log.debug("Start parsing '{}'", path);
      var bodyContentHandler = new BodyContentHandler(pipedWriter);
      autoDetectParser.parse(fis, bodyContentHandler, metadata);
    } catch (TikaException | IOException | SAXException e) {
      log.error("Error while parsing '{}'", path, e);
      exceptionWhileParsing.set(e);
    } finally {
      log.debug("End parsing '{}'", path);
      state.set(State.FINISHED);
      try {
        pipedWriter.close();
      } catch (IOException e) {
        log.error("While closing writer", e);
      }
    }
  }
}
