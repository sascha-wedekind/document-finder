package com.bytedompteur.documentfinder.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class LuceneQueryUtil {

    // Lucene special characters that need to be escaped:
    // + - && || ! ( ) { } [ ] ^ " ~ * ? : \ /
    // Note: The backslash \ itself must be escaped by prepending another backslash.
    // The forward slash / is also special in Lucene queries (e.g. for regex)
    private static final Set<Character> LUCENE_SPECIAL_CHARACTERS = new HashSet<>(Arrays.asList(
            '+', '-', '&', '|', '!', '(', ')', '{', '}', '[', ']', '^', '"', '~', '*', '?', ':', '\\', '/'
    ));

    /**
     * A private constructor to prevent instantiation of this utility class.
     */
    private LuceneQueryUtil() {
        // This constructor is intentionally empty.
    }

    /**
     * Escapes Lucene special characters in a query string.
     *
     * @param query The input query string.
     * @return The sanitized query string with special characters escaped,
     *         or the original query if it's null or empty.
     */
    public static String sanitizeQuery(String query) {
        if (query == null || query.isEmpty()) {
            return query;
        }

        StringBuilder sb = new StringBuilder();
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
