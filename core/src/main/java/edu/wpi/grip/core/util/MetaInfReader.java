package edu.wpi.grip.core.util;

import com.google.common.annotations.VisibleForTesting;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Reads class registries from the META-INF directory.
 */
public final class MetaInfReader {

  private MetaInfReader() {
    throw new UnsupportedOperationException("This is a utility class!");
  }

  /**
   * Reads all classes from the appropriate registry file.
   *
   * @param fileName the name of the file, relative to the META-INF directory
   * @param <T>      the type of the class (eg {@code Operation} for a registry of operation
   *                 implementations)
   * @return a stream of the classes in the registry file
   * @throws IOException if the file could not be read
   */
  public static <T> Stream<Class<T>> readClasses(String fileName) throws IOException {
    return readLines(fileName)
        .map(MetaInfReader::classForNameOrNull)
        .map(c -> (Class<T>) c)
        .filter(Objects::nonNull);
  }

  /**
   * Read all lines from a file located at {@code /META-INF/$fileName}.
   *
   * @param fileName the name of the file, relative to the META-INF directory
   * @throws IOException if no file exists with the given name, or if the file exists but cannot be
   *                     read
   * @see #readLines(InputStream)
   */
  public static Stream<String> readLines(String fileName) throws IOException {
    checkNotNull(fileName, "fileName");
    InputStream stream = MetaInfReader.class.getResourceAsStream("/META-INF/" + fileName);
    if (stream == null) {
      throw new IOException("No resource /META-INF/" + fileName + " found");
    }
    return readLines(stream);
  }

  /**
   * Reads all lines from an input stream. Comments are supported by starting with the {@code #}
   * character; everything after will be ignored. Empty lines and lines consisting of just a comment
   * are ignored. Whitespace at the start and end of each line wil be removed.
   *
   * @param inputStream the input stream to read lines from
   * @return a stream of the filtered lines
   * @throws IOException if the stream could not be read from or safely closed
   */
  @VisibleForTesting
  static Stream<String> readLines(InputStream inputStream) throws IOException {
    List<String> lines;
    try (InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
         BufferedReader bufferedReader = new BufferedReader(reader)) {
      lines = bufferedReader.lines()
          .map(MetaInfReader::stripComment)
          .map(String::trim)
          .filter(line -> !line.isEmpty())
          .collect(Collectors.toList());
    }
    return lines.stream();
  }

  @VisibleForTesting
  @SuppressWarnings("unchecked")
  static <T> Class<T> classForNameOrNull(String name) {
    try {
      return (Class<T>) Class.forName(name); // Assumes the caller knows what they're doing
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  /**
   * Strips a comment from a line. Comments are the section of the string after and including the
   * first '#' character in the line. Whitespace before that character will not be removed. If a
   * line does not contain a comment, it is returned with no modification.
   *
   * @param line the line to strip a comment from
   * @return the stripped line
   */
  @VisibleForTesting
  static String stripComment(String line) {
    int index = line.indexOf('#');
    if (index == -1) {
      return line;
    } else {
      return line.substring(0, index);
    }
  }

}
