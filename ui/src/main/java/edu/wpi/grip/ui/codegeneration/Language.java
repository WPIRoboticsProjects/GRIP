package edu.wpi.grip.ui.codegeneration;

import org.python.icu.impl.IllegalIcuArgumentException;

/**
 * An enum representing code generation languages.
 * Each language has a name as well as a directory for templates.
 */
public enum Language {
  JAVA("Java", "java"), CPP("C++", "cpp"), PYTHON("Python", "python");
  public final String name;
  public final String filePath;

  Language(String name, String filePath) {
    this.filePath = filePath;
    this.name = name;
  }
  
  /**
   * Returns a language based on the given name.
   * If given name does not match any of the Languages,
   * an IllegalIcuArgumentException is thrown.
   * @param name the language name to get the enum for.
   * @return the Language that represents the name.
   */
  public static Language get(String name) {
    if (name.equals(JAVA.name)) {
      return JAVA;
    } else if (name.equals(CPP.name)) {
      return CPP;
    } else if (name.equals(PYTHON.name)) {
      return PYTHON;
    } else {
      throw new IllegalArgumentException(name + " is not a valid name");
    }
  }

  public String toString() {
    return name;
  }

}