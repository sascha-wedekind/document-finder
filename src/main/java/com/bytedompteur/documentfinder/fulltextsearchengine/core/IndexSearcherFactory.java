package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import lombok.RequiredArgsConstructor;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.IndexSearcher;

import jakarta.inject.Inject;
import java.io.IOException;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
public class IndexSearcherFactory {

  private final IndexWriter indexWriter;
  private final IndexManager indexManager;

  public IndexSearcher build() throws IOException {
    return new IndexSearcher(indexManager.buildIndexReader(indexWriter));
  }
}
