package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.classic.ClassicTokenizerFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.de.GermanAnalyzer;
import org.apache.lucene.analysis.de.GermanStemFilterFactory;
import org.apache.lucene.analysis.en.KStemFilterFactory;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class PayloadAnalyzerFactory {

    public static final String[] WORD_DELIMITER_CONFIG = {"generateNumberParts", "1",
        "splitOnCaseChange", "1",
        "splitOnNumerics", "1",
        "generateWordParts", "1",
        "preserveOriginal", "1"};
    private final Map<Locale, Analyzer> cachedAnalyzers = new ConcurrentHashMap<>();

    Optional<Analyzer> buildAnalyzer(Locale locale) throws IOException {
        return Optional
            .ofNullable(locale)
            .map(it -> cachedAnalyzers.computeIfAbsent(it, this::createAnalyzer));

    }

    private Analyzer createAnalyzer(Locale locale) {
        return switch (locale.getLanguage()) {
            case "de" -> createGermanAnalyzer();
            case "en" -> createEnglishAnalyzer();
            default -> null;
        };
    }


    protected Analyzer createGermanAnalyzer() {
        try {
            return CustomAnalyzer
                .builder()
                .withTokenizer(ClassicTokenizerFactory.NAME)
                .addTokenFilter(WordDelimiterGraphFilterFactory.NAME, WORD_DELIMITER_CONFIG)
                .addTokenFilter(GermanStemFilterFactory.NAME)
                .addTokenFilter(LowerCaseFilterFactory.NAME)
//                .addTokenFilter(StopFilterFactory.NAME, "ignoreCase", "true", "words", "/org/apache/lucene/analysis/snowball/german_stop.txt", "format", "snowball")
                .addTokenFilter(StopFilterFactory.NAME, "ignoreCase", "true", "words", "analysis/snowball/german_stop.txt", "format", "snowball")
//                .addTokenFilter(SnowballPorterFilterFactory.NAME, "language", "German")
                .build();
        } catch (IOException e) {
            log.error("While generating german analyzer", e);
            return null;
        }
    }

    protected Analyzer createEnglishAnalyzer() {
        try {
            return CustomAnalyzer
                .builder()
                .withTokenizer(ClassicTokenizerFactory.NAME)
                .addTokenFilter(WordDelimiterGraphFilterFactory.NAME, WORD_DELIMITER_CONFIG)
                .addTokenFilter(LowerCaseFilterFactory.NAME)
                .addTokenFilter(KStemFilterFactory.NAME)
                .addTokenFilter(StopFilterFactory.NAME, "ignoreCase", "true") // Uses english stop words by default
                .build();
        } catch (IOException e) {
            log.error("While generating german analyzer", e);
            return null;
        }
    }
}
