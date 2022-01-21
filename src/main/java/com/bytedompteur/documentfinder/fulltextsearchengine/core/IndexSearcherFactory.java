package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

import javax.inject.Inject;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IndexSearcherFactory {

  private final IndexReader indexReader;

  public IndexSearcher build() {
    return new IndexSearcher(indexReader);
  }
}
