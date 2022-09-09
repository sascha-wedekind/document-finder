package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.SearchResult;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.MultiReader;
import org.apache.lucene.search.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
class IndexRepositoryTest {

  @Mock
  IndexSearcher mockedIndexSearcher;

  @Mock
  IndexSearcherFactory mockedIndexSearchFactory;

  @Mock
  IndexWriter mockedIndexWriter;

  @InjectMocks
  IndexRepository sut;

  @Test
  void loadDocumentFromIndexSearcher_returnsEmptyOptional_whenDocumentCouldNotBeFound() throws IOException {
    // Arrange
    Mockito
      .when(mockedIndexSearcher.doc(eq(123), anySet()))
      .thenReturn(null);

    // Act
    var result = sut.loadDocumentFromIndexSearcher(mockedIndexSearcher, 123);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void loadDocumentFromIndexSearcher_returnsEmptyOptional_whenIndexSearcherThrows() throws IOException {
    // Arrange
    Mockito
      .when(mockedIndexSearcher.doc(eq(123), anySet()))
      .thenThrow(new IOException("Expected exception from unit test"));

    // Act
    var result = sut.loadDocumentFromIndexSearcher(mockedIndexSearcher, 123);

    // Assert
    assertThat(result).isEmpty();
  }

  @Test
  void loadDocumentFromIndexSearcher_returnsOptionalContainingDocument_whenDocumentCouldBeFound() throws IOException {
    // Arrange
    Mockito
      .when(mockedIndexSearcher.doc(eq(123), anySet()))
      .thenReturn(new Document());

    // Act
    var result = sut.loadDocumentFromIndexSearcher(mockedIndexSearcher, 123);

    // Assert
    assertThat(result).isNotEmpty();
  }

  @Test
  void createSearchResultFlux_returnsNonNullPathsContainedInDocuments_whenScoreDocsNotEmpty() throws IOException {
    // Arrange
    var document1 = new Document();
    document1.add(new StringField("path", "a/b/c", Field.Store.NO));
    var document2 = new Document();
    document2.add(new StringField("path", "d/e/f", Field.Store.NO));

    Mockito
      .when(mockedIndexSearcher.doc(anyInt(), Mockito.anySet()))
      .thenReturn(document1) // for ScoreDoc 1
      .thenReturn(null) // for ScoreDoc 3
      .thenReturn(document2); // for ScoreDoc 3

    var scoreDocs = List.of(
      new ScoreDoc(1, 1.0F),
      new ScoreDoc(2, 1.0F),
      new ScoreDoc(3, 1.0F)
    );

    // Act
    var result = StepVerifier.create(sut.createSearchResultFlux(mockedIndexSearcher, scoreDocs.toArray(new ScoreDoc[0])));

    // Assert
    result
      .expectNext(SearchResult.builder().path(Path.of("a/b/c")).build())
      .expectNext(SearchResult.builder().path(Path.of("d/e/f")).build())
      .verifyComplete();
  }

  @Test
  void createSearchResultFlux_returnsEmptyFlux_whenScoreDocsIsNull() {
    // Act
    var result = StepVerifier.create(sut.createSearchResultFlux(mockedIndexSearcher, null));

    // Assert
    result.verifyComplete();
  }

  @Test
  void createSearchResultFlux_returnsEmptyFlux_whenScoreDocsIsEmpty() {
    // Act
    var result = StepVerifier.create(sut.createSearchResultFlux(mockedIndexSearcher, new ScoreDoc[0]));

    // Assert
    result.verifyComplete();
  }

  @Test
  void findByFileNameOrContent_returnsEmptyFlux_whenSearchTextIsNull() {
    // Act
    var result = StepVerifier.create(sut.findByFileNameOrContent(null));

    // Assert
    result.verifyComplete();
  }

  @Test
  void findByFileNameOrContent_returnsEmptyFlux_whenSearchTextIsEmpty() {
    // Act
    var result = StepVerifier.create(sut.findByFileNameOrContent("    "));

    // Assert
    result.verifyComplete();
  }

  @Test
  void findByFileNameOrContent_returnsEmptyFlux_whenIndexSearcherThrows() throws IOException {
    // Arrange
    Mockito
      .when(mockedIndexSearchFactory.build())
      .thenReturn(mockedIndexSearcher);

    Mockito
      .when(mockedIndexSearcher.search(any(), anyInt(), any(Sort.class)))
      .thenThrow(new IOException("Expected exception from unit test"));

    // Act
    var result = StepVerifier.create(sut.findByFileNameOrContent("a search text"));

    // Assert
    result.verifyComplete();
  }

  @Test
  void findByFileNameOrContent_returnsFlux_whenIndexSearcherReturnsWithoutAnError() throws IOException {
    // Arrange
    var document1 = new Document();
    document1.add(new StringField("path", "a/b/c", Field.Store.NO));
    var document2 = new Document();
    document2.add(new StringField("path", "d/e/f", Field.Store.NO));

    var scoreDocs = new ScoreDoc[]{
      new ScoreDoc(1, 1.0F),
      new ScoreDoc(2, 1.0F)
    };

    Mockito
      .when(mockedIndexSearcher.doc(anyInt(), Mockito.anySet()))
      .thenReturn(document1) // for ScoreDoc 1
      .thenReturn(document2); // for ScoreDoc 2

    Mockito
      .when(mockedIndexSearchFactory.build())
      .thenReturn(mockedIndexSearcher);

    Mockito
      .when(mockedIndexSearcher.search(any(), anyInt(), any(Sort.class)))
      .thenReturn(new TopFieldDocs(new TotalHits(2L, TotalHits.Relation.EQUAL_TO), scoreDocs, new SortField[0]));

    Mockito
      .when(mockedIndexSearcher.getIndexReader())
      .thenReturn(new MultiReader());

    // Act
    var result = StepVerifier.create(sut.findByFileNameOrContent("a search text"));

    // Assert
    result
      .expectNext(SearchResult.builder().path(Path.of("a/b/c")).build())
      .expectNext(SearchResult.builder().path(Path.of("d/e/f")).build())
      .verifyComplete();
  }
}
