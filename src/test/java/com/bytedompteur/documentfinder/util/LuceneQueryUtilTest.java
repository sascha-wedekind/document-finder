package com.bytedompteur.documentfinder.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class LuceneQueryUtilTest {

    @Test
    void testSanitize_nullInput() {
        assertNull(LuceneQueryUtil.sanitizeQuery(null), "Null input should return null.");
    }

    @Test
    void testSanitize_emptyInput() {
        assertEquals("", LuceneQueryUtil.sanitizeQuery(""), "Empty input should return empty string.");
    }

    @Test
    void testSanitize_noSpecialChars() {
        String query = "This is a normal query";
        assertEquals(query, LuceneQueryUtil.sanitizeQuery(query), "Query with no special chars should remain unchanged.");
    }

    // Test each individual special character
    // + - && || ! ( ) { } [ ] ^ " ~ * ? : \ /
    // Note: && and || are typically handled as two separate characters by such sanitizers.
    // The current LuceneQueryUtil escapes '&' and '|' individually.
    @ParameterizedTest
    @ValueSource(chars = {'+', '-', '&', '|', '!', '(', ')', '{', '}', '[', ']', '^', '"', '~', '*', '?', ':', '\\', '/'})
    void testSanitize_singleSpecialChar(char specialChar) {
        String query = "query" + specialChar + "suffix";
        String expected = "query\\" + specialChar + "suffix";
        assertEquals(expected, LuceneQueryUtil.sanitizeQuery(query), "Failed for special character: " + specialChar);
    }

    @Test
    void testSanitize_singleSpecialChar_logicalAnd() {
        String query = "query&&suffix"; // Test "&&" specifically if it were a single token
        // Current sanitizer treats & individually: query\& \&suffix
        String expected = "query\\&\\&suffix";
        assertEquals(expected, LuceneQueryUtil.sanitizeQuery(query));
    }

    @Test
    void testSanitize_singleSpecialChar_logicalOr() {
        String query = "query||suffix"; // Test "||" specifically
        // Current sanitizer treats | individually: query\| \|suffix
        String expected = "query\\|\\|suffix";
        assertEquals(expected, LuceneQueryUtil.sanitizeQuery(query));
    }


    @Test
    void testSanitize_multipleSpecialChars() {
        String query = "find+(me)-if:you*can?";
        String expected = "find\\+\\(me\\)\\-if\\:you\\*can\\?";
        assertEquals(expected, LuceneQueryUtil.sanitizeQuery(query));
    }

    @Test
    void testSanitize_allSpecialChars() {
        // Using the list: + - & | ! ( ) { } [ ] ^ " ~ * ? : \ /
        String query = "+-&|!(){}[]^\"~*?:\\/";
        String expected = "\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\\"\\~\\*\\?\\:\\\\\\/"; // Backslash needs double escaping in Java string literal for regex, then for string itself.
        assertEquals(expected, LuceneQueryUtil.sanitizeQuery(query));
    }

    @Test
    void testSanitize_allSpecialCharsStringCorrected() {
        String query = "+-&|!(){}[]^\"~*?:\\/";
        // Expected: \+\-\&\&\|\|!\(\)\{\}\[\]\^\"\~\*\?\:\\\/ (if && and || were distinct tokens)
        // Based on current implementation (individual char escaping):
        String expected = "\\+\\-\\&\\|\\!\\(\\)\\{\\}\\[\\]\\^\\\"\\~\\*\\?\\:\\\\\\/";
        // Let's verify the construction of 'expected' carefully.
        // Original: +  -  &  |  !  (  )  {  }  [  ]  ^  "  ~  *  ?  :  \  /
        // Escaped: \+ \- \& \| \! \( \) \{ \} \[ \] \^ \" \~ \* \? \: \\ \/
        // Java Str: \\+ \\- \\& \\| \\! \\( \\) \\{ \\} \\[ \\] \\^ \\" \\~ \\* \\? \\: \\\\ \\/
        assertEquals(expected, LuceneQueryUtil.sanitizeQuery(query));
    }


    @Test
    void testSanitize_backslashAtEnd() {
        String query = "query\\";
        String expected = "query\\\\"; // Escaped backslash
        assertEquals(expected, LuceneQueryUtil.sanitizeQuery(query));
    }

    @Test
    void testSanitize_specialCharsAtStartAndEnd() {
        String query = "*query*";
        String expected = "\\*query\\*";
        assertEquals(expected, LuceneQueryUtil.sanitizeQuery(query));

        String query2 = "!another!";
        String expected2 = "\\!another\\!";
        assertEquals(expected2, LuceneQueryUtil.sanitizeQuery(query2));
    }

    @Test
    void testSanitize_exampleFromRequirements() {
        String query = "(1+1):2";
        String expected = "\\(1\\+1\\)\\:2";
        assertEquals(expected, LuceneQueryUtil.sanitizeQuery(query));
    }

    @Test
    void testSanitize_complexQueryWithWordsAndSpecialChars() {
        String query = "Search for documents (important OR critical) AND title:\"urgent update\" BUT NOT status:closed";
        // ( ) & | are special. : " are special. + - could be implied by AND/NOT depending on parser, but we escape them.
        // Assuming LuceneQueryUtil escapes based on its defined set: + - & | ! ( ) { } [ ] ^ " ~ * ? : \ /
        String expected = "Search for documents \\(important OR critical\\) AND title\\:\\\"urgent update\\\" BUT NOT status\\:closed";
        // Rationale:
        // ( becomes \(
        // ) becomes \)
        // : becomes \:
        // " becomes \"
        // Other words and spaces remain. The sanitizer does not interpret AND/OR/NOT as special, only individual characters.
        assertEquals(expected, LuceneQueryUtil.sanitizeQuery(query));
    }
}
