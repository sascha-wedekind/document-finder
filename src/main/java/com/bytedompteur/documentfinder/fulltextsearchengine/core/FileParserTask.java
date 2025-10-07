package com.bytedompteur.documentfinder.fulltextsearchengine.core;


import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.language.detect.LanguageConfidence;
import org.apache.tika.language.detect.LanguageHandler;
import org.apache.tika.language.detect.LanguageResult;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class FileParserTask implements Runnable {

    private BodyContentHandler bodyContentHandler;
    private LanguageResult languageResult;

    public enum State {UNUSED, RUNNING, FINISHED}

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


    public Optional<Locale> getDetectedLanguage() {
        return Optional
            .ofNullable(languageResult.getLanguage())
            .map(Locale::of);
    }

    /**
     * @return true when the language confidence of Tika is high, otherwise false.
     */
    public boolean isLanguageDetectionReliable() {
        return languageResult.getConfidence() == LanguageConfidence.HIGH;
    }

    @Override
    public void run() {
        state.set(State.RUNNING);
        try (var fis = new BufferedInputStream(new FileInputStream(path.toFile()))) {
            log.info("Start parsing '{}'", path);
            parse(fis);
        } catch (Exception e) {
            log.error("Error while parsing '{}'", path, e);
            exceptionWhileParsing.set(e);
        } finally {
            log.info("End parsing '{}'. File identified as '{}'. Language: {} with confidence {}", path, metadata.get(Metadata.CONTENT_TYPE), languageResult.getLanguage(), languageResult.getConfidence());
            state.set(State.FINISHED);
        }
    }

    protected void parse(InputStream fis) throws IOException, SAXException, TikaException {
        bodyContentHandler = new BodyContentHandler(-1);
        var languageHandler = new LanguageHandler();
        var teeContentHandler = new TeeContentHandler(bodyContentHandler, languageHandler);
        autoDetectParser.parse(fis, teeContentHandler, metadata);
        languageResult = languageHandler.getLanguage();
    }
}
