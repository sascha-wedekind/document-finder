package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import com.bytedompteur.documentfinder.fulltextsearchengine.adapter.in.SearchResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.document.Field.Store;
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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class IndexRepository {

  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
  public static final int MAX_RESULT_LIMIT = 150;
  public static final int MAX_RESULT_LIMIT_LAST_UPDATED = 20;
  public static final String PAYLOAD_FIELD_NAME = "payload";
  public static final String PATH_FIELD_NAME = "path";
  public static final String UPDATED_FIELD_NAME = "updated";
  public static final String CREATED_FIELD_NAME = "created";
  public static final String CREATED_DISPLAY_FIELD_NAME = "createdDisplay";
  public static final String UPDATED_DISPLAY_FIELD_NAME = "updatedDisplay";
  public static final Set<String> FIELDS_TO_LOAD_FROM_INDEX = Set.of(
    PATH_FIELD_NAME,
    CREATED_DISPLAY_FIELD_NAME,
    UPDATED_DISPLAY_FIELD_NAME
  );
  public static final Pattern ALPHANUMERIC = Pattern.compile("^[a-zA-Z0-9]*$");

  private final IndexWriter indexWriter;
  private final IndexSearcherFactory indexSearcherFactory;

  public void save(FileRecord value) throws IOException {
    var pathString = value.path().toString();
    var idField = new StringField("id", pathString, Store.NO);
    var pathField = new TextField(PATH_FIELD_NAME, pathString, Store.YES);
    var payloadField = new TextField(PAYLOAD_FIELD_NAME, value.payloadReader());
    var createdField = new NumericDocValuesField(
      CREATED_FIELD_NAME,
      toLuceneDate(value.created())
    );
    var createdDisplayField = new StringField(
      CREATED_DISPLAY_FIELD_NAME,
      DATE_FORMATTER.format(value.created().atZone(ZoneId.systemDefault())),
      Store.YES
    );
    var updatedField = new NumericDocValuesField(
      UPDATED_FIELD_NAME,
      toLuceneDate(value.updated())
    );
    var updatedDisplayField = new StringField(
      UPDATED_DISPLAY_FIELD_NAME,
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

  private static Long toLuceneDate(Instant instant) {
    return Long.parseLong(DateTools.dateToString(Date.from(instant), DateTools.Resolution.SECOND));
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
          query = createPathContainsOrBodyContainsPrefixedWordQuery(searchTextString.toLowerCase());
        }
        result = executeSearch(query, MAX_RESULT_LIMIT);
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

  private Query createPathContainsOrBodyContainsPrefixedWordQuery(String searchTextString) {
    Query query;
    var prefixQuery = new PrefixQuery(new Term(PAYLOAD_FIELD_NAME, searchTextString));
    var regexpQuery = new RegexpQuery(new Term(PATH_FIELD_NAME, String.format(".*%s.*", searchTextString)));
    query = new BooleanQuery.Builder()
      .add(regexpQuery, BooleanClause.Occur.SHOULD)
      .add(prefixQuery, BooleanClause.Occur.SHOULD)
      .build();
    return query;
  }

  public Flux<SearchResult> findLastUpdated() {
    Flux<SearchResult> result = Flux.empty();
    try {
      result = executeSearch(new MatchAllDocsQuery(), MAX_RESULT_LIMIT_LAST_UPDATED);
    } catch (IOException e) {
      log.error("Failed to search for last updated files", e);
    }
    return result;
  }

  private Flux<SearchResult> executeSearch(
    Query query,
    int maxResultLimit
  ) throws IOException {
    var sortFieldUpdated = new SortField(UPDATED_FIELD_NAME, SortField.Type.LONG, true);
    var sortFieldCreate = new SortField(CREATED_FIELD_NAME, SortField.Type.LONG, true);
    var indexSearcher = indexSearcherFactory.build();
    var docs = indexSearcher.search(query, maxResultLimit, new Sort(sortFieldUpdated, sortFieldCreate));
    return createSearchResultFlux(indexSearcher, docs.scoreDocs)
      .doOnComplete(() -> closeReader(indexSearcher))
      .doOnCancel(() -> closeReader(indexSearcher));
  }

  private Query createQueryFromParseSearchText(String searchText) throws ParseException {
    Query query;
    var parser = new MultiFieldQueryParser(new String[]{PATH_FIELD_NAME, PAYLOAD_FIELD_NAME}, new StandardAnalyzer());
    parser.setDefaultOperator(QueryParser.Operator.OR);
    query = parser.parse(searchText);
    return query;
  }

  private boolean isQuerySearchTextSupposedForParser(String searchTextString) {
    return !ALPHANUMERIC.matcher(searchTextString).matches();
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
      .ofNullable(it.get(CREATED_DISPLAY_FIELD_NAME))
      .map(DATE_FORMATTER::parse)
      .map(LocalDateTime::from)
      .orElse(null);

    var updated = Optional
      .ofNullable(it.get(UPDATED_DISPLAY_FIELD_NAME))
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
      document = Optional.ofNullable(indexSearcher.doc(documentId, FIELDS_TO_LOAD_FROM_INDEX));
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
