package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.ByteBuffersDirectory;
import org.apache.lucene.store.Directory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class IndexRepositoryIntegrationTest {

    private IndexWriter indexWriter;
    private Directory fsDirectory;
    private IndexRepository sut;
    private IndexSearcherFactory mockedIndexSearchFactory;

    @BeforeEach
    void setUp() throws IOException {
        fsDirectory = new ByteBuffersDirectory();
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        indexWriter = new IndexWriter(fsDirectory, config);
        mockedIndexSearchFactory = Mockito.mock(IndexSearcherFactory.class);
        sut = new IndexRepository(indexWriter, mockedIndexSearchFactory);
    }

    @AfterEach
    void tearDown() throws IOException {
        if (indexWriter != null) {
            indexWriter.close();
        }
        if (fsDirectory != null) {
            for (String it : fsDirectory.listAll()) {
                fsDirectory.deleteFile(it);
            }
            fsDirectory.close();
        }
    }

    private static Stream<Arguments> provideFindByWithWildcardTestParameters() {
        return Stream.of(
            Arguments.of("familybusiness AND payment", 1),
            Arguments.of("familybusiness OR payment", 1),
            Arguments.of("familyb* AND payment", 1),
            Arguments.of("familyb* OR payment", 1),
            Arguments.of("familybusiness AND paym*", 1),
            Arguments.of("familybusiness OR paym*", 1),
            Arguments.of("*business AND payment", 1),
            Arguments.of("*business OR payment", 1),
            Arguments.of("*business AND paym*", 1),
            Arguments.of("*business OR paym*", 1),
            Arguments.of("*business AND paymQQ*", 0),
            Arguments.of("businessQQ OR paymQQ*", 0)
        );
    }

    @ParameterizedTest
    @MethodSource("provideFindByWithWildcardTestParameters")
    void findByFileNameOrContent_withWildcard(String searchText, int numberOfExpectedResults) throws IOException {
        // Arrange
        when(mockedIndexSearchFactory.build()).thenAnswer(param -> new IndexSearcher(DirectoryReader.open(fsDirectory)));

        FileRecord r1 = new FileRecord(Path.of("/a/1.pdf"), new StringReader(TestText.TEXT_ENGLISH), Locale.ENGLISH, Instant.ofEpochSecond(1L), Instant.ofEpochSecond(1L));
        sut.save(r1);
        indexWriter.flush();
        indexWriter.commit();
        indexWriter.close();

        // Act
        var result = StepVerifier.create(sut.findByFileNameOrContent(searchText));

        // Assert
        result.expectNextCount(numberOfExpectedResults).verifyComplete();
    }

    @Test
    void findLastUpdated_returnsAllDocumentsFromTheIndex_whenIndexSizeIsSmallerThanResultLimit() throws IOException {
        // Arrange
        for (int i = 1; i < 11; i++) {
            Document documentWithId = createDocumentWithId(i);
            indexWriter.addDocument(documentWithId);
        }
        indexWriter.flush();
        indexWriter.commit();
        when(mockedIndexSearchFactory.build()).thenReturn(new IndexSearcher(DirectoryReader.open(indexWriter)));

        // Act
        var firstStep = StepVerifier.create(sut.findLastUpdated());

        // Assert
        firstStep.expectNextCount(10)
            .verifyComplete();
    }

    @Test
    void findLastUpdated_returnsDocumentsLimitedByMaxResultLimit_whenIndexSizeIsGreaterThanResultLimit() throws IOException {
        // Arrange
        for (int i = 1; i < IndexRepository.MAX_RESULT_LIMIT_LAST_UPDATED + 5; i++) {
            Document documentWithId = createDocumentWithId(i);
            indexWriter.addDocument(documentWithId);
        }
        indexWriter.flush();
        indexWriter.commit();
        when(mockedIndexSearchFactory.build()).thenReturn(new IndexSearcher(DirectoryReader.open(indexWriter)));

        // Act
        var firstStep = StepVerifier.create(sut.findLastUpdated());

        // Assert
        firstStep.expectNextCount(IndexRepository.MAX_RESULT_LIMIT_LAST_UPDATED)
            .verifyComplete();
    }

    @Test
    void findLastUpdated_returnsNothing_whenIndexIsEmpty() throws IOException {
        // Arrange
        when(mockedIndexSearchFactory.build()).thenReturn(new IndexSearcher(DirectoryReader.open(indexWriter)));

        // Act
        var firstStep = StepVerifier.create(sut.findLastUpdated());

        // Assert
        firstStep.expectNextCount(0)
            .verifyComplete();
    }

    @Test
    void findLastUpdated_returnsOnlyNonDeletedDocuments_whenIndexContainsDeletedDocuments() throws IOException {
        // Arrange
        for (int i = 1; i <= 10; i++) {
            Document documentWithId = createDocumentWithId(i);
            indexWriter.addDocument(documentWithId);
        }
        indexWriter.flush();
        indexWriter.commit();

        for (int i = 1; i <= 10; i = i + 2) {
            var idTerm = new Term("id", Integer.toString(i));
            indexWriter.deleteDocuments(idTerm);
        }
        indexWriter.flush();
        indexWriter.commit();

        when(mockedIndexSearchFactory.build()).thenReturn(new IndexSearcher(DirectoryReader.open(indexWriter)));

        // Act
        var firstStep = StepVerifier.create(sut.findLastUpdated());

        // Assert
        firstStep.expectNextCount(5)
            .verifyComplete();
    }

    @Test
    void findByFileNameOrContent_returnsTheResultOrderedByLatestUpdatedFilesFirst() throws IOException {
        // Arrange
        when(mockedIndexSearchFactory.build()).thenAnswer(param -> new IndexSearcher(DirectoryReader.open(fsDirectory)));

        FileRecord r1 = new FileRecord(Path.of("/a/1.pdf"), new StringReader(" pdf "), Locale.ENGLISH, Instant.ofEpochSecond(1L), Instant.ofEpochSecond(1L));
        FileRecord r2 = new FileRecord(Path.of("/a/2.pdf"), new StringReader(" pdf "), Locale.ENGLISH, Instant.ofEpochSecond(2L), Instant.ofEpochSecond(2L));
        FileRecord r3 = new FileRecord(Path.of("/a/3.pdf"), new StringReader(" pdf "), Locale.ENGLISH, Instant.ofEpochSecond(3L), Instant.ofEpochSecond(3L));
        FileRecord r4 = new FileRecord(Path.of("/a/4.pdf"), new StringReader(" pdf "), Locale.ENGLISH, Instant.ofEpochSecond(4L), Instant.ofEpochSecond(4L));

        var records = new ArrayList<>(List.of(r1, r2, r3, r4));
        Collections.shuffle(records); // Shuffle to make sure the order is mixed up

        IndexRepository indexRepository = sut;
        for (FileRecord record : records) {
            indexRepository.save(record);
        }
        indexWriter.flush();
        indexWriter.commit();
        indexWriter.close();


        // Act
        var firstStep = StepVerifier.create(sut.findByFileNameOrContent("pdf"));

        // Assert
        firstStep
            .assertNext(it -> assertThat(it.getPath()).isEqualTo(Path.of("/a/4.pdf")))
            .assertNext(it -> assertThat(it.getPath()).isEqualTo(Path.of("/a/3.pdf")))
            .assertNext(it -> assertThat(it.getPath()).isEqualTo(Path.of("/a/2.pdf")))
            .assertNext(it -> assertThat(it.getPath()).isEqualTo(Path.of("/a/1.pdf")))
            .verifyComplete();
    }

    @Test
    void findLastUpdated_returnsTheResultOrderedByLatestUpdatedFilesFirst() throws IOException {
        // Arrange
        when(mockedIndexSearchFactory.build()).thenAnswer(param -> new IndexSearcher(DirectoryReader.open(fsDirectory)));

        FileRecord r1 = new FileRecord(Path.of("/a/1.pdf"), new StringReader(" pdf "), Locale.ENGLISH, Instant.ofEpochSecond(1L), Instant.ofEpochSecond(1L));
        FileRecord r2 = new FileRecord(Path.of("/a/2.pdf"), new StringReader(" pdf "), Locale.ENGLISH, Instant.ofEpochSecond(2L), Instant.ofEpochSecond(2L));
        FileRecord r3 = new FileRecord(Path.of("/a/3.pdf"), new StringReader(" pdf "), Locale.ENGLISH, Instant.ofEpochSecond(3L), Instant.ofEpochSecond(3L));
        FileRecord r4 = new FileRecord(Path.of("/a/4.pdf"), new StringReader(" pdf "), Locale.ENGLISH, Instant.ofEpochSecond(4L), Instant.ofEpochSecond(4L));

        var records = new ArrayList<>(List.of(r1, r2, r3, r4));
        Collections.shuffle(records); // Shuffle to make sure the order is mixed up

        IndexRepository indexRepository = sut;
        for (FileRecord record : records) {
            indexRepository.save(record);
        }
        indexWriter.flush();
        indexWriter.commit();
        indexWriter.close();


        // Act
        var firstStep = StepVerifier.create(sut.findLastUpdated());

        // Assert
        firstStep
            .assertNext(it -> assertThat(it.getPath()).isEqualTo(Path.of("/a/4.pdf")))
            .assertNext(it -> assertThat(it.getPath()).isEqualTo(Path.of("/a/3.pdf")))
            .assertNext(it -> assertThat(it.getPath()).isEqualTo(Path.of("/a/2.pdf")))
            .assertNext(it -> assertThat(it.getPath()).isEqualTo(Path.of("/a/1.pdf")))
            .verifyComplete();
    }

    private static Document createDocumentWithId(int index) {
        var idField = new StringField("id", Integer.toString(index), Field.Store.NO);
        var valueField = new TextField("fieldWithValue", String.format("Field value %s", index), Field.Store.YES);
        var document = new Document();
        document.add(idField);
        document.add(valueField);
        return document;
    }
}
