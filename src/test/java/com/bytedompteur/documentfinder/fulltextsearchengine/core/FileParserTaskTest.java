package com.bytedompteur.documentfinder.fulltextsearchengine.core;

import org.apache.commons.io.input.ReaderInputStream;
import org.apache.tika.exception.TikaException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.Locale;
import java.util.stream.Stream;

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
        Assertions.assertThat(sut.getDetectedLanguage()).contains(Locale.of(expectedLanguage));
    }
}