package edu.wpi.grip.ui.util;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility for fuzzy text searching
 */
public class SearchUtility {

    /**
     * @return true if query if approximately a substring of text, not taking into account capitalization, whitespace,
     * or minor typos
     */
    public static boolean fuzzyContains(String text, String query) {
        if (query.isEmpty()) return true;
        if (text.isEmpty()) return false;

        // Normalize the capitalization and whitespace (which are currently both pretty inconsistent among operations)
        text = text.toUpperCase().replaceAll("[^a-zA-Z]", "");
        query = query.toUpperCase().replaceAll("[^a-zA-Z]", "");

        if (query.length() <= text.length()) {
            // Assuming the search string is smaller than the name of the operation, show it if the search string is very
            // close to a substring of the operation name.
            final int substrLength = query.length();
            final int substrCount = text.length() - query.length() + 1;

            for (int i = 0; i < substrCount; i++) {
                final String subname = text.substring(i, i + substrLength);
                if (StringUtils.getLevenshteinDistance(query, subname, 1) != -1) return true;
            }

            return false;
        } else {
            // Otherwise, just look at the distance of the two strings
            return StringUtils.getLevenshteinDistance(query, text, 1) != -1;
        }
    }
}
