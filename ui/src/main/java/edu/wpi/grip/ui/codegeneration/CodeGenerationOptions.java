package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.settings.ProjectSettings;

import java.io.File;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Holds options for code generation.
 */
public class CodeGenerationOptions {

  private final Language language;
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
  private CodeGenerationOptions(Language language,
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

  public Language getLanguage() {
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

  /**
   * Copies these options to project settings.
   *
   * @param projectSettings the project settings to copy into
   */
  public void copyTo(ProjectSettings projectSettings) {
    projectSettings.setPreferredGeneratedLanguage(language.name);
    projectSettings.setGeneratedPipelineName(className);
    projectSettings.setCodegenDestDir(new File(saveDir));
    projectSettings.setGeneratedJavaPackage(packageName);
    projectSettings.setGeneratedPythonModuleName(moduleName);
    projectSettings.setImplementWpilibPipeline(implementWpilibPipeline);
  }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private Language language;
    private String className;
    private Boolean implementVisionPipeline;
    private String saveDir;
    private String packageName;
    private String moduleName;

    private Builder() {
    }

    public Builder language(Language language) {
      this.language = checkNotNull(language);
      return this;
    }

    public Builder className(String className) {
      this.className = checkNotNull(className);
      return this;
    }

    public Builder saveDir(String saveDir) {
      this.saveDir = checkNotNull(saveDir);
      return this;
    }

    public Builder implementVisionPipeline(boolean implementVisionPipeline) {
      this.implementVisionPipeline = implementVisionPipeline;
      return this;
    }

    public Builder packageName(String packageName) {
      this.packageName = checkNotNull(packageName);
      return this;
    }

    public Builder moduleName(String moduleName) {
      this.moduleName = checkNotNull(moduleName);
      return this;
    }

    /**
     * Builds a new {@code CodeGenerationOptions} object. This ensures that every required
     * option has been set.
     */
    public CodeGenerationOptions build() {
      return new CodeGenerationOptions(
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
