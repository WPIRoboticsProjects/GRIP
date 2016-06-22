package edu.wpi.grip.ui.codegeneration;

import org.python.icu.impl.IllegalIcuArgumentException;

public enum Language {
  JAVA("Java", "java"), CPP("C++", "cpp"), PYTHON("Python", "python");
  public final String name;
  public final String filePath;

  Language(String name, String filePath) {
    this.filePath = filePath;
    this.name = name;
  }

  public static Language get(String name) {
    if (name.equals(JAVA.name)) {
      return JAVA;
    } else if (name.equals(CPP.name)) {
      return CPP;
    } else if (name.equals(PYTHON.name)) {
      return PYTHON;
    } else {
      throw new IllegalIcuArgumentException("not a valid name");
    }
  }

  public String toString() {
    return name;
  }

}