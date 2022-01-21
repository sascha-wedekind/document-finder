package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class IndexRepository {

  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
  public static final int MAX_RESULT_LIMIT = 100;

  private final IndexWriter indexWriter;
  private final IndexSearcherFactory indexSearcherFactory;

  public void save(FileRecord value) throws IOException {
    var pathString = value.path().toString();
    var idField = new StringField("id", pathString, Store.NO);
    var pathField = new TextField("path", pathString, Store.YES);
    var payloadField = new TextField("payload", value.payloadReader());
    var createdField = new LongPoint(
      "created",
      value.created().toEpochMilli()
    );
    var createdDisplayField = new StringField(
      "createdDisplay",
      DATE_FORMATTER.format(value.created().atZone(ZoneId.systemDefault())),
      Store.YES
    );
    var updatedField = new LongPoint(
      "updated",
      value.updated().toEpochMilli()
    );
    var updatedDisplayField = new StringField(
      "updatedDisplay",
      DATE_FORMATTER.format(value.updated().atZone(ZoneId.systemDefault())),
      Store.YES
    );

    Document document = new Document();
    document.add(idField);
    document.add(pathField);
    document.add(payloadField);
    document.add(createdField);
    document.add(createdDisplayField);
    document.add(updatedField);
    document.add(updatedDisplayField);

    log.debug("Adding entry for '{}'", pathString);
    var idTerm = new Term("id", pathString);
    indexWriter.deleteDocuments(idTerm);
    indexWriter.addDocument(document);
    log.debug("Entry for '{}' added", pathString);
  }

  public Flux<Path> findByFileNameOrContent(CharSequence searchText) {
    Flux<Path> result = Flux.empty();
    if (isNotBlank(searchText)) {
      try {
        var parser = new MultiFieldQueryParser(new String[]{"path", "payload"}, new StandardAnalyzer());
        var query = parser.parse(searchText.toString());
        var indexSearcher = indexSearcherFactory.build();
        var docs = indexSearcher.search(query, MAX_RESULT_LIMIT);
        result = createSearchResultFlux(indexSearcher, docs.scoreDocs);
      } catch (ParseException | IOException e) {
        log.error("Failed to search for '{}'", searchText, e);
      }
    }
    return result;
  }

  protected Flux<Path> createSearchResultFlux(IndexSearcher indexSearcher, ScoreDoc[] scoreDocs) {
    //noinspection OptionalGetWithoutIsPresent
    return Mono
      .justOrEmpty(scoreDocs)
      .flatMapMany(Flux::fromArray)
      .map(it -> it.doc)
      .map(it -> loadDocumentFromIndexSearcher(indexSearcher, it))
      .filter(Optional::isPresent)
      .map(it -> it.get().get("path"))
      .map(Path::of);
  }

  protected Optional<Document> loadDocumentFromIndexSearcher(IndexSearcher indexSearcher, int documentId) {
    Optional<Document> document = Optional.empty();
    try {
      document = Optional.ofNullable(indexSearcher.doc(documentId, Set.of("path")));
    } catch (IOException e) {
      log.error("Could not load document with id {} from index searcher", documentId, e);
    }
    return document;
  }


  public void delete(Path path) throws IOException {
    log.debug("Deleting '{}' from index", path);
    var idTerm = new Term("id", path.toString());
    indexWriter.deleteDocuments(idTerm);
    log.debug("'{}' from index deleted", path);
  }

  public int count() {
    return indexWriter.getDocStats().numDocs;
  }

  public void commit() {
    try {
      indexWriter.commit();
      log.debug("Changes committed");
    } catch (IOException e) {
      log.error("Failed to commit", e);
    }
  }
}
