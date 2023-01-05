package com.bytedompteur.documentfinder.fulltextsearchengine.core;


import lombok.extern.slf4j.Slf4j;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class FileParserTask implements Runnable {

  private BodyContentHandler bodyContentHandler;

  public enum State{UNUSED,RUNNING, FINISHED}

  private final Path path;
  private final AutoDetectParser autoDetectParser = new AutoDetectParser();
  private final Metadata metadata = new Metadata();
  private final AtomicReference<Exception> exceptionWhileParsing = new AtomicReference<>(null);
  private final AtomicReference<State> state = new AtomicReference<>(State.UNUSED);

  private FileParserTask(Path path) {
    this.path = path;
  }

  @SuppressWarnings("java:S2095")
  public static FileParserTask create(Path path) {
    return new FileParserTask(path);
  }

  public Reader getReader() {
    return new StringReader(bodyContentHandler.toString());
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
    try (var fis = new BufferedInputStream(new FileInputStream(path.toFile()))) {
      log.info("Start parsing '{}'", path);
      bodyContentHandler = new BodyContentHandler(-1);
      autoDetectParser.parse(fis, bodyContentHandler, metadata);
    } catch (Exception e) {
      log.error("Error while parsing '{}'", path, e);
      exceptionWhileParsing.set(e);
    } finally {
      log.info("End parsing '{}'. File identified as '{}'", path, metadata.get(Metadata.CONTENT_TYPE));
      state.set(State.FINISHED);
    }
  }
}
