package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
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
  public static final String PAYLOAD_FIELD_NAME = "payload";
  public static final String PATH_FIELD_NAME = "path";

  private final IndexWriter indexWriter;
  private final IndexSearcherFactory indexSearcherFactory;

  public void save(FileRecord value) throws IOException {
    var pathString = value.path().toString();
    var idField = new StringField("id", pathString, Store.NO);
    var pathField = new TextField(PATH_FIELD_NAME, pathString, Store.YES);
    var payloadField = new TextField(PAYLOAD_FIELD_NAME, value.payloadReader());
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
    indexWriter.commit();
    log.debug("Entry for '{}' added", pathString);
  }

  public Flux<SearchResult> findByFileNameOrContent(CharSequence searchText) {
    Flux<SearchResult> result = Flux.empty();
    if (isNotBlank(searchText)) {
      try {
        Query query;
        var searchTextString = searchText.toString();
        if (isQuerySearchTextSupposedForParser(searchTextString)) {
          query = createQueryFromParseSearchText(searchTextString);
        } else {
          query = createPathContainsOrBodyConainsPrefixedWordQuery(searchTextString.toLowerCase());
        }
        var indexSearcher = indexSearcherFactory.build();
        var docs = indexSearcher.search(query, MAX_RESULT_LIMIT);
        result = createSearchResultFlux(indexSearcher, docs.scoreDocs);
        result
          .doOnComplete(() -> closeReader(indexSearcher))
          .doOnCancel(() -> closeReader(indexSearcher));
      } catch (ParseException | IOException e) {
        log.error("Failed to search for '{}'", searchText, e);
      }
    }
    return result;
  }

  private void closeReader(IndexSearcher indexSearcher) {
    try {
      indexSearcher.getIndexReader().close();
    } catch (IOException e) {
      log.error("While closing index reader", e);
    }
  }

  private Query createPathContainsOrBodyConainsPrefixedWordQuery(String searchTextString) {
    Query query;
    var prefixQuery = new PrefixQuery(new Term(PAYLOAD_FIELD_NAME, searchTextString));
    var regexpQuery = new RegexpQuery(new Term(PATH_FIELD_NAME, String.format(".*%s.*", searchTextString)));
    query = new BooleanQuery.Builder()
      .add(regexpQuery, BooleanClause.Occur.SHOULD)
      .add(prefixQuery, BooleanClause.Occur.SHOULD)
      .build();
    return query;
  }

  private Query createQueryFromParseSearchText(String searchText) throws ParseException {
    Query query;
    var parser = new MultiFieldQueryParser(new String[]{PATH_FIELD_NAME, PAYLOAD_FIELD_NAME}, new StandardAnalyzer());
    parser.setDefaultOperator(QueryParser.Operator.OR);
    query = parser.parse(searchText);
    return query;
  }

  private boolean isQuerySearchTextSupposedForParser(String searchTextString) {
    return searchTextString.contains("AND")
      || searchTextString.contains("OR")
      || searchTextString.contains("(")
      || searchTextString.contains(")")
      || searchTextString.contains("[")
      || searchTextString.contains("]")
      || searchTextString.contains(":");
  }

  protected Flux<SearchResult> createSearchResultFlux(IndexSearcher indexSearcher, ScoreDoc[] scoreDocs) {
    return Mono
      .justOrEmpty(scoreDocs)
      .flatMapMany(Flux::fromArray)
      .map(it -> it.doc)
      .map(it -> loadDocumentFromIndexSearcher(indexSearcher, it))
      .flatMap(Mono::justOrEmpty)
      .map(this::toSearchResult);
  }

  private SearchResult toSearchResult(Document it) {
    var path = Optional
      .ofNullable(it.get(PATH_FIELD_NAME))
      .map(Path::of)
      .orElse(null);

    var created = Optional
      .ofNullable(it.get("createdDisplay"))
      .map(DATE_FORMATTER::parse)
      .map(LocalDateTime::from)
      .orElse(null);

    var updated = Optional
      .ofNullable(it.get("updatedDisplay"))
      .map(DATE_FORMATTER::parse)
      .map(LocalDateTime::from)
      .orElse(null);

    return SearchResult
      .builder()
      .path(path)
      .fileCreated(created)
      .fileLastUpdated(updated)
      .build();
  }

  protected Optional<Document> loadDocumentFromIndexSearcher(IndexSearcher indexSearcher, int documentId) {
    Optional<Document> document = Optional.empty();
    try {
      document = Optional.ofNullable(indexSearcher.doc(documentId, Set.of(PATH_FIELD_NAME)));
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

  public void clear() {
    try {
      log.info("Clearing index");
      indexWriter.deleteAll();
      indexWriter.commit();
      log.info("Index cleared");
    } catch (IOException e) {
      log.error("Failed to clear index", e);
    }
  }
}
