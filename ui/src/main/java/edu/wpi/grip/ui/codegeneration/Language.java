package edu.wpi.grip.ui.codegeneration;

public enum Language {
  JAVA("Java"), CPP("C++"), PYTHON("Python");
  public final String name;

  Language(String name) {
    this.name = name;
  }

  public static Language get(String name) {
    if (name.equals(JAVA.name))
      return JAVA;
    else if (name.equals(CPP.name))
      return CPP;
    else
      return PYTHON;
  }

  public String toString() {
    return name;
  }
}
