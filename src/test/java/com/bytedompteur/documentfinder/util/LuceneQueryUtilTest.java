package com.bytedompteur.documentfinder.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
// import org.junit.jupiter.params.provider.ValueSource; // No longer needed if MethodSource is used for single chars

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*; // AssertJ imports
// import static org.junit.jupiter.api.Assertions.*; // JUnit imports to be removed by overwrite

class LuceneQueryUtilTest {

    @Test
    void sanitizeQuery_shouldReturnNull_whenInputIsNull() {
        // arrange
        String query = null;

        // act
        String sanitizedQuery = LuceneQueryUtil.sanitizeQuery(query);

        // assert
        assertThat(sanitizedQuery).isNull();
    }

    @Test
    void sanitizeQuery_shouldReturnEmptyString_whenInputIsEmptyString() {
        // arrange
        String query = "";

        // act
        String sanitizedQuery = LuceneQueryUtil.sanitizeQuery(query);

        // assert
        assertThat(sanitizedQuery).isEmpty();
    }

    @Test
    void sanitizeQuery_shouldReturnUnchangedString_whenInputHasNoSpecialChars() {
        // arrange
        String query = "This is a normal query 123";

        // act
        String sanitizedQuery = LuceneQueryUtil.sanitizeQuery(query);

        // assert
        assertThat(sanitizedQuery).isEqualTo(query);
    }

    private static Stream<Character> specialCharactersProvider() {
        // Lucene special characters: + - && || ! ( ) { } [ ] ^ " ~ * ? : \ /
        // The characters & and | are treated individually by the current sanitizer.
        return Stream.of('+', '-', '&', '|', '!', '(', ')', '{', '}', '[', ']', '^', '"', '~', '*', '?', ':', '\\', '/');
    }

    @ParameterizedTest
    @MethodSource("specialCharactersProvider")
    void sanitizeQuery_shouldEscapeCharacter_whenInputContainsSingleSpecialCharacter(char specialChar) {
        // arrange
        String query = "prefix" + specialChar + "suffix";
        String expected = "prefix\\" + specialChar + "suffix";

        // act
        String sanitizedQuery = LuceneQueryUtil.sanitizeQuery(query);

        // assert
        assertThat(sanitizedQuery)
            .as("Failed for special character: '%s'", specialChar)
            .isEqualTo(expected);
    }

    @Test
    void sanitizeQuery_shouldEscapeIndividualAmpersandAndPipe_whenInputContainsDoubleAmpersandOrDoublePipe() {
        // arrange
        String queryAnd = "query&&suffix";
        String expectedAnd = "query\\&\\&suffix";
        String queryOr = "query||suffix";
        String expectedOr = "query\\|\\|suffix";

        // act
        String sanitizedAnd = LuceneQueryUtil.sanitizeQuery(queryAnd);
        String sanitizedOr = LuceneQueryUtil.sanitizeQuery(queryOr);

        // assert
        assertThat(sanitizedAnd).isEqualTo(expectedAnd)
            .as("Logical AND '&&' should be escaped as individual '&' characters.");
        assertThat(sanitizedOr).isEqualTo(expectedOr)
            .as("Logical OR '||' should be escaped as individual '|' characters.");
    }

    @Test
    void sanitizeQuery_shouldEscapeAllCharacters_whenInputContainsMultipleMixedSpecialChars() {
        // arrange
        String query = "find+(me)-if:you*can?";
        String expected = "find\\+\\(me\\)\\-if\\:you\\*can\\?";

        // act
        String sanitizedQuery = LuceneQueryUtil.sanitizeQuery(query);

        // assert
        assertThat(sanitizedQuery).isEqualTo(expected);
    }

    @Test
    void sanitizeQuery_shouldEscapeAllCharacters_whenInputIsSequenceOfAllSpecialChars() {
        // arrange
        String query = "+-&|!(){}[]^\"~*?:\\/";
        String expected = "\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\\"\\~\\*\\?\\:\\\\\\/";

        // act
        String sanitizedQuery = LuceneQueryUtil.sanitizeQuery(query);

        // assert
        assertThat(sanitizedQuery).isEqualTo(expected);
    }

    @Test
    void sanitizeQuery_shouldEscapeBackslash_whenInputEndsWithBackslash() {
        // arrange
        String query = "query\\";
        String expected = "query\\\\";

        // act
        String sanitizedQuery = LuceneQueryUtil.sanitizeQuery(query);

        // assert
        assertThat(sanitizedQuery).isEqualTo(expected);
    }

    @Test
    void sanitizeQuery_shouldEscapeCharacters_whenInputHasSpecialCharsAtStartAndEnd() {
        // arrange
        String query1 = "*query*";
        String expected1 = "\\*query\\*";
        String query2 = "!another!";
        String expected2 = "\\!another\\!";

        // act
        String sanitized1 = LuceneQueryUtil.sanitizeQuery(query1);
        String sanitized2 = LuceneQueryUtil.sanitizeQuery(query2);

        // assert
        assertThat(sanitized1).isEqualTo(expected1);
        assertThat(sanitized2).isEqualTo(expected2);
    }

    @Test
    void sanitizeQuery_shouldEscapeAllCharacters_whenInputContainsOnlySpecialChars() {
        // arrange
        String query = "()[]{}:"; // Note: { } were not in the original special char set, but are in the provider.
                                 // The provider is correct as per Lucene spec.
        String expected = "\\(\\)\\[\\]\\{\\}\\:";

        // act
        String sanitizedQuery = LuceneQueryUtil.sanitizeQuery(query);

        // assert
        assertThat(sanitizedQuery).isEqualTo(expected);
    }

    @Test
    void sanitizeQuery_shouldCorrectlyEscapeExampleQuery_fromTaskDescription() {
        // arrange
        String query = "(1+1):2";
        String expected = "\\(1\\+1\\)\\:2";

        // act
        String sanitizedQuery = LuceneQueryUtil.sanitizeQuery(query);

        // assert
        assertThat(sanitizedQuery).isEqualTo(expected);
    }
}
