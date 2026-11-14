package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

class IndexManagerTest {

    /**
     * The directory 'lucene-index-10.1.0' in the test resources contains an index with one document, written by Lucene
     * 10.1.0 (codec Lucene101). Newer Lucene versions can only read it with the library 'lucene-backward-codecs'.
     * Without it, existing users could no longer open their index.
     */
    @Test
    void buildIndexWriter_opensIndexOfOlderLuceneVersion_whenIndexWasWrittenByLucene101(@TempDir Path applicationHomeDirectory) throws IOException {
        // Arrange
        var indexDirectory = Files.createDirectories(applicationHomeDirectory.resolve("index"));
        for (var fileName : List.of("segments_1", "_0.si", "_0.cfe", "_0.cfs")) {
            try (var resource = getClass().getResourceAsStream("/lucene-index-10.1.0/" + fileName)) {
                Files.copy(Objects.requireNonNull(resource), indexDirectory.resolve(fileName));
            }
        }
        var sut = new IndexManager(applicationHomeDirectory.toString());
        sut.init();

        // Act
        int numberOfDocuments;
        try (var writer = sut.buildIndexWriter(); var reader = sut.buildIndexReader(writer)) {
            numberOfDocuments = reader.numDocs();
        }

        // Assert
        assertThat(numberOfDocuments).isEqualTo(1);
    }

    @Test
    void tmp() throws IOException {
        // Arrange
        var sut = IndexManager.createPerFieldAnalyzer();
        var text = "The email address is l.abc@google.de and the IBAN is DE1234567809. The customer number is 123/456SAB. The price is €8,500. We first met on April 23, 2011.";

        // Act
        var result = analyze(IndexRepository.PAYLOAD_FIELD_NAME_ENGLISH, text, sut);

        var resourceAsStream = this.getClass().getResourceAsStream("/org/apache/lucene/analysis/snowball/german_stop.txt");
            String content;
        try (resourceAsStream) {
            byte[] bytes = resourceAsStream.readAllBytes();
            content = new String(bytes, StandardCharsets.UTF_8);
        }



        // Assert
        assertThat(result).contains(
            "email",
            "l.abc@google.de",
            "de1234567809",
            "123/456sab"
        );
    }


    @Test
    void payloadFieldAnalyzer() throws IOException {
        // Arrange
        var sut = IndexManager.createPerFieldAnalyzer();
        var text = "Die EMail-Adresse ist l.abc@google.de und die IBAN lautet DE1234567809. Die Kundennummer ist 123/456SAB. Der Preis ist €8,500. Am 23.04.2011 haben wir uns zuerst getroffen.";

        // Act
        var result = analyze(IndexRepository.PAYLOAD_FIELD_NAME_GERMAN, text, sut);

        // Assert
        assertThat(result).contains(
            "email",
            "l.abc@google.de",
            "de1234567809",
            "123/456sab",
            "8,500",
            "23.04.2011"
        );
    }

    @Test
    void pathFieldAnalyzer() throws IOException {
        // Arrange
        var sut = IndexManager.createPerFieldAnalyzer();
        var text = "RE202111_AbC-Beitrag-November.pdf";

        // Act
        var result = analyze(IndexRepository.PATH_FIELD_NAME, text, sut);

        // Assert
        assertThat(result).contains(
            "re202111", "abc", "beitrag", "november", "pdf"
        );
    }

    public List<String> analyze(String fieldName, String text, Analyzer analyzer) throws IOException {
        List<String> result = new ArrayList<>();
        TokenStream tokenStream = analyzer.tokenStream(fieldName, text);
        CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            result.add(attr.toString());
        }
        return result;
    }
}
