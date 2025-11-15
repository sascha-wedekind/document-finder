package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.tika.exception.TikaException;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.InstanceOfAssertFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xml.sax.SAXException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class FileParserTaskTest {

    private static Stream<Arguments> provideTestParameter() {
        return Stream.of(
            Arguments.of(TestText.TEXT_ENGLISH, "en"),
            Arguments.of(TestText.TEXT_GERMAN, "de")
        );
    }


    @ParameterizedTest
    @MethodSource("provideTestParameter")
    void parseIdentifiesReaderContentLanguage(String text, String expectedLanguage) throws IOException, TikaException, SAXException {
        // Arrange
        var mockedPath = Mockito.mock(Path.class);
        var sut = FileParserTask.create(mockedPath);
        var readerInputStream = ReaderInputStream.builder().setReader(new StringReader(text)).get();

        // Act
        sut.parse(readerInputStream);

        // Assert
        assertThat(sut.getDetectedLanguage()).contains(Locale.of(expectedLanguage));
    }

    @Test
    void run_setsTaskStateInExpectedOrder() throws IOException {
        // Arrange
        var stateBeforeGettingInputStream = new AtomicReference<>(FileParserTask.State.UNUSED);
        var stateAfterGettingInputStream = new AtomicReference<>(FileParserTask.State.UNUSED);

        var mockedPath = Mockito.mock(Path.class);
        var mockedInputStream = Mockito.mock(InputStreamFactory.class);
        var sut = new FileParserTask(mockedPath, mockedInputStream);

        Mockito.when(mockedInputStream.create(Mockito.any())).then(new  Answer<InputStream>() {
            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                stateBeforeGettingInputStream.set(sut.getState());
                return null;
            }
        });
        assertThat(sut.getState()).isEqualTo(FileParserTask.State.UNUSED);

        // Act
        sut.run();

        // Assert
        stateAfterGettingInputStream.set(sut.getState());
        assertThat(sut)
            .extracting(FileParserTask::getExceptionThrownWhileParsing)
            .asInstanceOf(InstanceOfAssertFactories.OPTIONAL)
            .isNotEmpty();
        assertThat(stateBeforeGettingInputStream.get()).isEqualTo(FileParserTask.State.RUNNING);
        assertThat(stateAfterGettingInputStream.get()).isEqualTo(FileParserTask.State.FINISHED);
    }
}