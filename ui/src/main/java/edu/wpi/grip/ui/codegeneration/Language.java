package edu.wpi.grip.ui.codegeneration;

/**
 * An enum representing code generation languages.
 * Each language has a name as well as a directory for templates.
 */
public enum Language {

  JAVA("Java", "java", "java"),
  CPP("C++", "cpp", "cpp"),
  PYTHON("Python", "py", "python");

  public final String name;
  public final String extension;
  public final String filePath;

  Language(String name, String extension, String filePath) {
    this.name = name;
    this.extension = extension;
    this.filePath = filePath;
  }

  /**
   * Returns a language based on the given name. If given name does not match any of the Languages,
   * returns {@code null}.
   *
   * @param name the language name to get the enum for.
   *
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

  @Override
  public String toString() {
    return name;
  }

}
