package edu.wpi.grip.core.settings;

import edu.wpi.grip.core.GripFileManager;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Holds options for code generation.
 */
public class CodeGenerationSettings {

  private final String language;
  private final String className;
  private final boolean implementWpilibPipeline;
  private final String saveDir;
  private final String packageName;
  private final String moduleName;

  public static final String LANGUAGE = "language";
  public static final String CLASS_NAME = "className";
  public static final String IMPLEMENT_WPILIB_PIPELINE = "implementVisionPipeline";
  public static final String SAVE_DIR = "saveDir";
  public static final String PACKAGE_NAME = "packageName";
  public static final String MODULE_NAME = "moduleName";


  /**
   * The default code generation settings.
   * <br>
   * <table>
   * <tr><td>Language</td><td>Java</td></tr>
   * <tr><td>Class name</td><td>GripPipeline</td></tr>
   * <tr><td>Implement WPILib API</td><td>false</td></tr>
   * <tr><td>Save directory</td><td>User home</td></tr>
   * <tr><td>Java package</td><td>Default package</td></tr>
   * <tr><td>Python module</td><td>grip</td></tr>
   * </table>
   */
  public static final CodeGenerationSettings DEFAULT_SETTINGS = new CodeGenerationSettings();

  /**
   * Creates the default code generation settings.
   * <br>
   * <table>
   * <tr><td>Language</td><td>Java</td></tr>
   * <tr><td>Class name</td><td>GripPipeline</td></tr>
   * <tr><td>Implement WPILib API</td><td>false</td></tr>
   * <tr><td>Save directory</td><td>User home</td></tr>
   * <tr><td>Java package</td><td>Default package</td></tr>
   * <tr><td>Python module</td><td>grip</td></tr>
   * </table>
   */
  CodeGenerationSettings() {
    this("Java",
        "GripPipeline",
        false,
        GripFileManager.GRIP_DIRECTORY.getAbsolutePath(),
        "",
        "grip");
  }

  /**
   * Private constructor; use a builder.
   *
   * @param language                the language to generate to
   * @param className               the name of the class to generate
   * @param implementWpilibPipeline if the generated class should implement the
   *                                WPILib VisionPipeline interface
   * @param saveDir                 the directory to save the generated file to
   * @param packageName             the name of the Java package to place the file in
   * @param moduleName              the name of the Python module
   */
  private CodeGenerationSettings(String language,
                                 String className,
                                 boolean implementWpilibPipeline,
                                 String saveDir,
                                 String packageName,
                                 String moduleName) {
    this.language = language;
    this.className = className;
    this.implementWpilibPipeline = implementWpilibPipeline;
    this.saveDir = saveDir;
    this.packageName = packageName;
    this.moduleName = moduleName;
  }

  public String getLanguage() {
    return language;
  }

  public String getClassName() {
    return className;
  }

  public boolean shouldImplementWpilibPipeline() {
    return implementWpilibPipeline;
  }

  public String getSaveDir() {
    return saveDir;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getModuleName() {
    return moduleName;
  }

  public static Builder builder() {
    return new Builder();
  }

  /**
   * Creates a builder with defaults from the given settings.
   *
   * @param defaultSettings the default settings for the builder to use
   * @return a settings builder
   */
  public static Builder builder(CodeGenerationSettings defaultSettings) {
    return new Builder()
        .language(defaultSettings.getLanguage())
        .className(defaultSettings.getClassName())
        .implementVisionPipeline(defaultSettings.shouldImplementWpilibPipeline())
        .saveDir(defaultSettings.getSaveDir())
        .packageName(defaultSettings.getPackageName())
        .moduleName(defaultSettings.getModuleName());
  }

  public static final class Builder {

    private String language;
    private String className;
    private Boolean implementVisionPipeline;
    private String saveDir;
    private String packageName;
    private String moduleName;

    private Builder() {
    }

    /**
     * Sets the language. Must be one of "Java", "C++", "Python.
     */
    public Builder language(String language) {
      checkArgument(language.matches("Java|C\\+\\+|Python"));
      this.language = language;
      return this;
    }

    /**
     * Sets the generated class name.
     */
    public Builder className(String className) {
      this.className = checkNotNull(className);
      return this;
    }

    /**
     * Sets the directory code should be generated in.
     */
    public Builder saveDir(String saveDir) {
      this.saveDir = checkNotNull(saveDir);
      return this;
    }

    /**
     * Sets if the generated pipeline should implement the WPILib API.
     */
    public Builder implementVisionPipeline(boolean implementVisionPipeline) {
      this.implementVisionPipeline = implementVisionPipeline;
      return this;
    }

    /**
     * Sets the package of the generated Java class.
     */
    public Builder packageName(String packageName) {
      this.packageName = checkNotNull(packageName);
      return this;
    }

    /**
     * Sets the module name (also file name) of the generated Python class.
     */
    public Builder moduleName(String moduleName) {
      this.moduleName = checkNotNull(moduleName);
      return this;
    }

    /**
     * Builds a new {@code CodeGenerationSettings} object. This ensures that every required
     * option has been set.
     */
    public CodeGenerationSettings build() {
      return new CodeGenerationSettings(
          checkNotNull(language, LANGUAGE),
          checkNotNull(className, CLASS_NAME),
          checkNotNull(implementVisionPipeline, IMPLEMENT_WPILIB_PIPELINE),
          checkNotNull(saveDir, SAVE_DIR),
          checkNotNull(packageName, PACKAGE_NAME),
          checkNotNull(moduleName, MODULE_NAME)
      );
    }

  }

}
