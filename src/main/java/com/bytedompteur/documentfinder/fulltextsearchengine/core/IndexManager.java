package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.classic.ClassicAnalyzer;
import org.apache.lucene.analysis.classic.ClassicTokenizerFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
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
import java.util.Locale;
import java.util.Map;

import static java.nio.file.Files.notExists;

@RequiredArgsConstructor
@Getter
@Slf4j
public class IndexManager {

    private boolean initialized = false;
    private FSDirectory directory;
    private IndexWriterConfig writerConfig;
    private final String applicationHomeDir;
    private static final PayloadAnalyzerFactory payloadAnalyzerFactory = new PayloadAnalyzerFactory();

    public IndexWriter buildIndexWriter() throws IOException {
        return new IndexWriter(directory, writerConfig);
    }

    public IndexReader buildIndexReader(IndexWriter value) throws IOException {
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
            PerFieldAnalyzerWrapper perFieldAnalyzer = createPerFieldAnalyzer();
            writerConfig = new IndexWriterConfig(perFieldAnalyzer);
            log.info("Writer analyzer and configuration created");
        } catch (IOException e) {
            log.error("Could not create writer analyzer and configuration", e);
            throw e;
        }
    }

    protected static PerFieldAnalyzerWrapper createPerFieldAnalyzer() throws IOException {
        var defaultPayloadAnalyzer = createDefaultPayloadAnalyzer();
        return new PerFieldAnalyzerWrapper(new ClassicAnalyzer(), Map.of(
            IndexRepository.PATH_FIELD_NAME, createPathAnalyzer(),
            IndexRepository.PAYLOAD_FIELD_NAME_GERMAN, payloadAnalyzerFactory.buildAnalyzer(Locale.GERMAN).orElseGet(() -> {
                log.warn("Could not obtain german payload analyzer, using default analyzer");
                return defaultPayloadAnalyzer;
            }),
            IndexRepository.PAYLOAD_FIELD_NAME_ENGLISH, payloadAnalyzerFactory.buildAnalyzer(Locale.ENGLISH).orElseGet(() -> {
                log.warn("Could not obtain english payload analyzer, using default analyzer");
                return defaultPayloadAnalyzer;
            })
        ));
    }

    protected static Analyzer createPathAnalyzer() throws IOException {
        return CustomAnalyzer
            .builder()
            .withTokenizer(ClassicTokenizerFactory.NAME)
            .addTokenFilter(WordDelimiterGraphFilterFactory.NAME, "catenateWords", "0", "splitOnNumerics", "0", "splitOnCaseChange", "0")
            .addTokenFilter(LowerCaseFilterFactory.NAME)
            .build();
    }

    protected static Analyzer createDefaultPayloadAnalyzer() throws IOException {
        return CustomAnalyzer
            .builder()
            .withTokenizer(ClassicTokenizerFactory.NAME)
            .addTokenFilter(LowerCaseFilterFactory.NAME)
            .build();
    }

}
