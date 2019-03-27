package edu.wpi.grip.core.util;

import com.google.common.collect.ImmutableList;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class MetaInfReaderTest {

  @Test
  public void testTrimComment() {
    String line = "# This is a comment";
    String stripped = MetaInfReader.stripComment(line);
    assertEquals("Stripped comment should be empty", "", stripped);
  }

  @Test
  public void testTrimCommentAfterLine() {
    String line = "What I want to keep # What I don't want to keep";
    String stripped = MetaInfReader.stripComment(line);
    assertEquals("Unexpected stripped line", "What I want to keep ", stripped);
  }

  @Test
  public void testRead() throws IOException {
    String input = "# Leading comment\na\nb\n# Comment in the middle\n\nc\nd\ne # Trailing comment";
    List<String> expected = ImmutableList.of("a", "b", "c", "d", "e");
    InputStream s = new StringBufferInputStream(input);
    List<String> out = MetaInfReader.read(s)
        .collect(Collectors.toList());
    assertEquals("Unexpected line result", expected, out);
  }

  @Test
  public void testClassForName() {
    String name = "java.lang.Object";
    Class<?> clazz = MetaInfReader.classForNameOrNull(name);
    assertEquals("Object class should have worked", Object.class, clazz);
  }

  @Test
  public void testClassForNameNonExistent() {
    String name = "not.a.real.Type";
    Class<?> clazz = MetaInfReader.classForNameOrNull(name);
    assertNull("No class should exist with name '" + name + "'", clazz);
  }

  @Test
  public void testClassForNamePrivateInner() {
    String name = Inner.class.getName();
    Class<?> clazz = MetaInfReader.classForNameOrNull(name);
    assertEquals("A private inner class should be accessible", Inner.class, clazz);
  }

  private static final class Inner {
  }

}
