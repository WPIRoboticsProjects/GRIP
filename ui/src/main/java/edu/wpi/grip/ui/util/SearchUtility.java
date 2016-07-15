package edu.wpi.grip.ui.util;

import org.apache.commons.lang3.StringUtils;

import java.util.Locale;

/**
 * Utility for fuzzy text searching.
 */
public class SearchUtility {

  /**
   * Returns true if query if approximately a substring of text, not taking into account
   * capitalization, whitespace, or minor typos.
   *
   * @param text  The text to search.
   * @param query The text to use as a query.
   */
  @SuppressWarnings("PMD.AvoidReassigningParameters")
  public static boolean fuzzyContains(String text, String query) {
    if (query.isEmpty()) {
      return true;
    }
    if (text.isEmpty()) {
      return false;
    }

    // Normalize the capitalization and whitespace (which are currently both pretty inconsistent
    // among operations)
    text = text.toUpperCase(Locale.ENGLISH).replaceAll("[^a-zA-Z]", "");
    query = query.toUpperCase(Locale.ENGLISH).replaceAll("[^a-zA-Z]", "");

    if (query.length() <= text.length()) {
      // Assuming the search string is smaller than the name of the operation, show it if the
      // search string is very
      // close to a substring of the operation name.
      final int substrLength = query.length();
      final int substrCount = text.length() - query.length() + 1;

      for (int i = 0; i < substrCount; i++) {
        final String subname = text.substring(i, i + substrLength);
        if (StringUtils.getLevenshteinDistance(query, subname, 1) != -1) {
          return true;
        }
      }

      return false;
    } else {
      // Otherwise, just look at the distance of the two strings
      return StringUtils.getLevenshteinDistance(query, text, 1) != -1;
    }
  }
}
