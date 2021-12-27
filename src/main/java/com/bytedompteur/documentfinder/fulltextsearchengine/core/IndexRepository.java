package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import javax.inject.Inject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Slf4j
public class IndexRepository {

  public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  private final IndexWriter indexWriter;

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

//  public void findByPath(Path path) throws IOException {
////new Query()
////    indexWriter.deleteDocuments(new Term())
//
//    var indexReader = DirectoryReader.open(directory);
//    var indexSearcher = new IndexSearcher(indexReader);
//
//  }

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
