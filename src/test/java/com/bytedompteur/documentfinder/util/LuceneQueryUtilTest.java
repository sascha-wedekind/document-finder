package com.bytedompteur.documentfinder.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LuceneQueryUtilTest {

    @Test
    void testSanitize_nullInput_shouldReturnNull() {
        assertNull(LuceneQueryUtil.sanitizeQuery(null), "Null input should return null.");
    }

    @Test
    void testSanitize_emptyInput_shouldReturnEmpty() {
        assertEquals("", LuceneQueryUtil.sanitizeQuery(""), "Empty input should return an empty string.");
    }

    @Test
    void testSanitize_noSpecialChars_shouldReturnSame() {
        String query = "This is a normal query 123";
        assertEquals(query, LuceneQueryUtil.sanitizeQuery(query), "Query with no special chars should remain unchanged.");
    }

    // Method source for special characters
    private static Stream<Character> specialCharactersProvider() {
        // Lucene special characters: + - && || ! ( ) { } [ ] ^ " ~ * ? : \ /
        // The characters & and | are treated individually by the current sanitizer.
        return Stream.of('+', '-', '&', '|', '!', '(', ')', '{', '}', '[', ']', '^', '"', '~', '*', '?', ':', '\\', '/');
    }

    @ParameterizedTest
    @MethodSource("specialCharactersProvider")
    void testSanitize_singleSpecialChar_shouldBeEscaped(char specialChar) {
        String query = "prefix" + specialChar + "suffix";
        String expected = "prefix\\" + specialChar + "suffix";
        assertEquals(expected, LuceneQueryUtil.sanitizeQuery(query),
                "Failed for special character: " + specialChar + ". Expected: " + expected + ", Got: " + LuceneQueryUtil.sanitizeQuery(query));
    }

    @Test
    void testSanitize_logicalAndOr_shouldEscapeIndividualChars() {
        // Test "&&"
        assertEquals("query\\&\\&suffix", LuceneQueryUtil.sanitizeQuery("query&&suffix"),
                "Logical AND '&&' should be escaped as individual '&' characters.");

        // Test "||"
        assertEquals("query\\|\\|suffix", LuceneQueryUtil.sanitizeQuery("query||suffix"),
                "Logical OR '||' should be escaped as individual '|' characters.");
    }

    @Test
    void testSanitize_multipleSpecialChars_shouldAllBeEscaped() {
        String query = "find+(me)-if:you*can?";
        String expected = "find\\+\\(me\\)\\-if\\:you\\*can\\?";
        assertEquals(expected, LuceneQueryUtil.sanitizeQuery(query));
    }

    @Test
    void testSanitize_allSpecialCharsInSequence_shouldAllBeEscaped() {
        // Test string with all defined special characters: + - & | ! ( ) { } [ ] ^ " ~ * ? : \ /
        String query = "+-&|!(){}[]^\"~*?:\\/";
        // Expected escaped string: \+\-\&\|\!\(\)\{\}\[\]\^\"\~\*\?\:\\\/
        // Java string literal for the expected string (double backslashes):
        String expected = "\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\\"\\~\\*\\?\\:\\\\\\/";
        assertEquals(expected, LuceneQueryUtil.sanitizeQuery(query));
    }

    @Test
    void testSanitize_backslashAtEnd_shouldBeEscaped() {
        String query = "query\\";
        String expected = "query\\\\"; // Escaped backslash (in Java string: \\\\)
        assertEquals(expected, LuceneQueryUtil.sanitizeQuery(query));
    }

    @Test
    void testSanitize_specialCharsAtStartAndEnd_shouldBeEscaped() {
        String query1 = "*query*";
        String expected1 = "\\*query\\*";
        assertEquals(expected1, LuceneQueryUtil.sanitizeQuery(query1));

        String query2 = "!another!";
        String expected2 = "\\!another\\!";
        assertEquals(expected2, LuceneQueryUtil.sanitizeQuery(query2));
    }

    @Test
    void testSanitize_queryWithOnlySpecialChars_shouldBeFullyEscaped() {
        String query = "()[]{}:";
        String expected = "\\(\\)\\[\\]\\{\\}\\:"; // Corrected based on defined set { } are special
        assertEquals(expected, LuceneQueryUtil.sanitizeQuery(query));
    }

    @Test
    void testSanitize_exampleFromOriginalTask_shouldMatch() {
        // Original example: (1+1):2  ->  \(1\+1\)\:2
        String query = "(1+1):2";
        String expected = "\\(1\\+1\\)\\:2";
        assertEquals(expected, LuceneQueryUtil.sanitizeQuery(query));
    }
}
