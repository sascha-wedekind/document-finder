package com.bytedompteur.documentfinder.fulltextsearchengine.core;


import com.optimaize.langdetect.DetectedLanguage;
import com.optimaize.langdetect.LanguageDetector;
import com.optimaize.langdetect.LanguageDetectorBuilder;
import com.optimaize.langdetect.i18n.LdLocale;
import com.optimaize.langdetect.ngram.NgramExtractors;
import com.optimaize.langdetect.profiles.LanguageProfile;
import com.optimaize.langdetect.profiles.LanguageProfileReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class FileParserTask implements Runnable {

    private static final List<LdLocale> SUPPORTED_LANGUAGES = List.of(LdLocale.fromString("en"), LdLocale.fromString("de"));
    private static List<LanguageProfile> languageProfiles = null;

    static {
        try {
            languageProfiles = new LanguageProfileReader().readBuiltIn(SUPPORTED_LANGUAGES);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load language profiles", e);
        }
    }

    private static final ThreadLocal<LanguageDetector> DETECTOR_THREAD_LOCAL =
        ThreadLocal.withInitial(() -> {
            return LanguageDetectorBuilder
                .create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();
        });

    private final AtomicReference<StringReader> contentReader = new AtomicReference<>();

    public enum State {UNUSED, RUNNING, FINISHED}

    private final Path path;
    private final AtomicReference<Throwable> exceptionWhileParsing = new AtomicReference<>(null);
    private final AtomicReference<State> state = new AtomicReference<>(State.UNUSED);
    private final AtomicReference<Optional<Locale>> detectedLanguage = new AtomicReference<>(Optional.empty());
    private final InputStreamFactory streamFactory;

    private FileParserTask(Path path) {
        this(path, new InputStreamFactory());
    }

    protected FileParserTask(Path path, InputStreamFactory streamFactory) {
        this.path = path;
        this.streamFactory = streamFactory;
    }

    @SuppressWarnings("java:S2095")
    public static FileParserTask create(Path path) {
        return new FileParserTask(path);
    }

    public Reader getReader() {
        return contentReader.get();
    }

    public State getState() {
        return state.get();
    }

    public Optional<Throwable> getExceptionThrownWhileParsing() {
        return Optional.ofNullable(exceptionWhileParsing.get());
    }


    public Optional<Locale> getDetectedLanguage() {
        return detectedLanguage.get();
    }


    @Override
    public void run() {
        state.set(State.RUNNING);
        Metadata metadata = null;
        try (var fis = streamFactory.create(path)) {
            log.debug("Start parsing '{}'", path);
            metadata = parse(fis);
        } catch (Throwable e) {
            log.error("Error while parsing '{}'", path, e);
            exceptionWhileParsing.set(e);
        } finally {
            var language = getDetectedLanguage().map(Locale::toString).orElse("unkown");
            var contentType = Optional.ofNullable(metadata).map(it -> it.get(Metadata.CONTENT_TYPE)).orElse("unkown");
            log.debug("End parsing '{}'. File identified as '{}'. Language: {}", path, contentType, language);
            state.set(State.FINISHED);
        }
    }

    protected Metadata parse(InputStream fis) throws IOException, SAXException, TikaException {
        var metadata = new Metadata();
        var autoDetectParser = new AutoDetectParser();
        var bodyContentHandler = new BodyContentHandler(-1); // No content limit
        autoDetectParser.parse(fis, bodyContentHandler, metadata);
        var content = bodyContentHandler.toString();
        detectLanguage(content);
        contentReader.set(new StringReader(content));
        return metadata;
    }

    protected void detectLanguage(String content) {
        log.debug("Detecting langauge for content of size {}", content.length());
        var detect = DETECTOR_THREAD_LOCAL.get().getProbabilities(content);
        log.debug("Detected languages '{}'", detect);
        detectedLanguage.set(Optional.ofNullable(detect)
            .orElse(List.of())
            .stream()
            .filter(it -> SUPPORTED_LANGUAGES.contains(it.getLocale()))
            .findFirst()
            .map(DetectedLanguage::getLocale)
            .map(LdLocale::getLanguage)
            .map(Locale::of)
        );
        DETECTOR_THREAD_LOCAL.remove();
    }
}
