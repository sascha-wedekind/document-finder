package com.bytedompteur.documentfinder.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class LuceneQueryUtil {

    // Lucene special characters that need to be escaped based on Lucene documentation:
    // + - && || ! ( ) { } [ ] ^ " ~ * ? : \ /
    // Note: The problem states "&&" and "||" as special. Standard Lucene escaping usually
    // handles & and | individually. The implementation will escape individual characters
    // from the provided set. If "&&" or "||" were meant as atomic tokens to be escaped
    // differently, the requirement would need clarification. This implementation
    // will escape '&' and '|' if they appear.
    private static final Set<Character> LUCENE_SPECIAL_CHARACTERS = new HashSet<>(Arrays.asList(
            '+', '-', '&', '|', '!', '(', ')', '{', '}', '[', ']', '^', '"', '~', '*', '?', ':', '\\', '/'
    ));

    /**
     * A private constructor to prevent instantiation of this utility class.
     */
    private LuceneQueryUtil() {
        // This constructor is intentionally empty to prevent instantiation.
    }

    /**
     * Escapes Lucene special characters in a query string.
     * <p>
     * Special characters are: + - && || ! ( ) { } [ ] ^ " ~ * ? : \ /
     * Each special character will be prepended with a backslash (\).
     *
     * @param query The input query string.
     * @return The sanitized query string with special characters escaped,
     *         or the original query if it's null or empty.
     */
    public static String sanitizeQuery(String query) {
        if (query == null || query.isEmpty()) {
            return query;
        }

        StringBuilder sb = new StringBuilder(query.length() + 5); // Initial capacity with some room
        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            if (LUCENE_SPECIAL_CHARACTERS.contains(c)) {
                sb.append('\\');
            }
            sb.append(c);
        }
        return sb.toString();
    }
}
