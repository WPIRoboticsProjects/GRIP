package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.settings.ProjectSettings;

import java.io.File;
import java.util.HashMap;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Holds options for code generation.
 */
public class CodeGenerationOptions extends HashMap<String, Object> {

  public static final String LANGUAGE = "language";
  public static final String CLASS_NAME = "className";
  public static final String IMPLEMENT_WPILIB_PIPELINE = "implementVisionPipeline";
  public static final String SAVE_DIR = "saveDir";
  public static final String PACKAGE_NAME = "packageName";
  public static final String MODULE_NAME = "moduleName";

  private CodeGenerationOptions() {
    // Use builder() instead
  }

  public Language getLanguage() {
    return (Language) get(LANGUAGE);
  }

  public String getClassName() {
    return (String) get(CLASS_NAME);
  }

  public boolean implementWpilibPipeline() {
    return (boolean) get(IMPLEMENT_WPILIB_PIPELINE);
  }

  public String getSaveDir() {
    return (String) get(SAVE_DIR);
  }

  public String getPackageName() {
    return (String) get(PACKAGE_NAME);
  }

  public String getModuleName() {
    return (String) get(MODULE_NAME);
  }

  /**
   * Copies these options to project settings.
   *
   * @param projectSettings the project settings to copy into
   */
  public void copyTo(ProjectSettings projectSettings) {
    projectSettings.setPreferredGeneratedLanguage(getLanguage().name);
    projectSettings.setGeneratedPipelineName(getClassName());
    projectSettings.setCodegenDestDir(new File(getSaveDir()));
    projectSettings.setGeneratedJavaPackage(getPackageName());
    projectSettings.setGeneratedPythonModuleName(getModuleName());
    projectSettings.setImplementWpilibPipeline(implementWpilibPipeline());
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

    public Builder() {
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
      CodeGenerationOptions options = new CodeGenerationOptions();
      options.put(LANGUAGE, checkNotNull(language, LANGUAGE));
      options.put(CLASS_NAME, checkNotNull(className, CLASS_NAME));
      options.put(IMPLEMENT_WPILIB_PIPELINE,
          checkNotNull(implementVisionPipeline, IMPLEMENT_WPILIB_PIPELINE));
      options.put(SAVE_DIR, checkNotNull(saveDir, SAVE_DIR));
      options.put(PACKAGE_NAME, checkNotNull(packageName, PACKAGE_NAME));
      options.put(MODULE_NAME, checkNotNull(moduleName, MODULE_NAME));
      return options;
    }

  }

}
