package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceTokenizerFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class PlaygroundTest {

  private String FIELD_NAME = "fieldName";

  @Test
  void standardAnalyzer() throws IOException {
    List<String> result = analyze("RE202111_RuV-Beitrag-November.pdf", new StandardAnalyzer());
    result.forEach(System.out::println);
  }

  @Test
  void stopAnalyzer() throws IOException {
    List<String> result = analyze("RE202111_RuV-Beitrag-November.pdf", new StopAnalyzer((CharArraySet) null));
    result.forEach(System.out::println);
  }

  @Test
  void simpleAnalyzer() throws IOException {
    List<String> result = analyze("RE202111_RuV-Beitrag-November.pdf", new SimpleAnalyzer());
    result.forEach(System.out::println);
  }

  @Test
  void wordDelimiterGraphFilterAnalyzer() throws IOException {
    var customAnalyzer = CustomAnalyzer
      .builder()
      .withTokenizer(WhitespaceTokenizerFactory.NAME)
      .addTokenFilter(WordDelimiterGraphFilterFactory.NAME, "catenateWords", "1")
      .build();

    List<String> result = analyze("RE202111_RuV-Beitrag-November.pdf", customAnalyzer);
    result.forEach(System.out::println);
  }

  public List<String> analyze(String text, Analyzer analyzer) throws IOException {
    List<String> result = new ArrayList<String>();
    TokenStream tokenStream = analyzer.tokenStream(FIELD_NAME, text);
    CharTermAttribute attr = tokenStream.addAttribute(CharTermAttribute.class);
    tokenStream.reset();
    while(tokenStream.incrementToken()) {
      result.add(attr.toString());
    }
    return result;
  }
}
