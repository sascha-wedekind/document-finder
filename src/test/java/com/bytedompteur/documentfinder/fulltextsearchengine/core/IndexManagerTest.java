package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IndexManagerTest {

  @Test
  void payloadFieldAnalyzer() throws IOException {
    // Arrange
    var sut = IndexManager.createPerFieldAnalyzer();
    var text = "Die EMail-Adresse ist l.abc@google.de und die IBAN lautet DE1234567809. Die Kundennummer ist 123/456SAB.";

    // Act
    var result = analyze(IndexRepository.PAYLOAD_FIELD_NAME, text, sut);

    // Assert
    assertThat(result).contains(
      "email",
      "l.abc@google.de",
      "de1234567809",
      "123/456sab"
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
    while(tokenStream.incrementToken()) {
      result.add(attr.toString());
    }
    return result;
  }
}
