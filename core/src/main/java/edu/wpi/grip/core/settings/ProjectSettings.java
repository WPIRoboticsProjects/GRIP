package edu.wpi.grip.core.settings;

import com.google.common.base.MoreObjects;
import com.google.common.base.Throwables;

import java.io.File;

import javax.annotation.Nonnegative;
import javax.annotation.Nullable;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * This object holds settings that are saved in project files.  This includes things like team
 * numbers, which need to be preserved when deploying the project.
 */
@SuppressWarnings("JavadocMethod")
public class ProjectSettings implements Settings, Cloneable {

  @Setting(label = "FRC team number", description = "The team number, if used for FRC")
  private int teamNumber = 0;

  @Setting(label = "NetworkTables server address", description = "The host that runs the "
      + "NetworkTables server. If not specified and NetworkTables is used, the hostname is derived "
      + "from the team number.")
  private String publishAddress = computeFRCAddress(teamNumber);

  @Setting(label = "Deploy address", description = "The remote host that grip should be deployed "
      + "to. If not specified, the hostname is derived from the team number.")
  private String deployAddress = computeFRCAddress(teamNumber);

  @Setting(label = "Deploy directory", description = "The directory on the remote host to deploy "
      + "GRIP to.")
  private String deployDir = "/home/lvuser";

  @Setting(label = "Deploy user", description = "The username to log in with when deploying over "
      + "SSH.")
  private String deployUser = "lvuser";

  @Setting(label = "Deploy Java home", description = "Where Java is installed on the robot.")
  private String deployJavaHome = "/usr/local/frc/JRE/";

  @Setting(label = "Deploy JVM options", description = "Command line options passed to the "
      + "roboRIO JVM")
  private String deployJvmOptions = "-Xmx50m -XX:-OmitStackTraceInFastThrow "
      + "-XX:+HeapDumpOnOutOfMemoryError -XX:MaxNewSize=16m";

  @Setting(label = "Preferred generated language",
           description = "The preferred language to generate code for")
  private String preferredGeneratedLanguage = "";

  @Setting(label = "Generate WPILib vision API",
           description = "")
  private boolean implementWpilibPipeline = false;

  @Setting(label = "Generated pipeline name",
           description = "The name of the generated pipeline class")
  private String generatedPipelineName = "Pipeline";

  @Setting(label = "Generated Python module name",
           description = "The name of the generated Python module")
  private String generatedPythonModuleName = "grip";

  @Setting(label = "Generated Java package name",
           description = "The name of the package the generated Java file is in")
  private String generatedJavaPackage = "";

  @Setting(label = "Code generation destination directory",
           description = "The directory where generated code should be saved")
  private File codegenDestDir = new File(System.getProperty("user.home") + "/GRIP");


  // Getters and setters


  public int getTeamNumber() {
    return teamNumber;
  }

  /**
   * Set the FRC team number.  If the deploy address and NetworkTables server address haven't been
   * manually overridden, this also changes them to the mDNS hostname of the team's roboRIO.
   */
  public void setTeamNumber(@Nonnegative int teamNumber) {
    checkArgument(teamNumber >= 0, "Team number cannot be negative");

    final String oldFrcAddress = computeFRCAddress(this.teamNumber);
    final String newFrcAddress = computeFRCAddress(teamNumber);

    this.teamNumber = teamNumber;

    // If the deploy address and/or NetworkTables server address was previously the default for
    // the old team number (ie: it was roborio-xxx-frc.local), update it with the new team number
    if (oldFrcAddress.equals(getDeployAddress())) {
      setDeployAddress(newFrcAddress);
    }

    if (oldFrcAddress.equals(getPublishAddress())) {
      setPublishAddress(newFrcAddress);
    }
  }

  public String getPublishAddress() {
    return publishAddress;
  }

  public void setPublishAddress(@Nullable String publishAddress) {
    if (publishAddress != null) {
      this.publishAddress = publishAddress;
    }
  }

  public String getDeployAddress() {
    return deployAddress;
  }

  public void setDeployAddress(@Nullable String deployAddress) {
    if (deployAddress != null) {
      this.deployAddress = deployAddress;
    }
  }

  public String getDeployDir() {
    return deployDir;
  }

  public void setDeployDir(@Nullable String deployDir) {
    if (deployDir != null) {
      this.deployDir = deployDir;
    }
  }

  public String getDeployUser() {
    return deployUser;
  }

  public void setDeployUser(@Nullable String deployUser) {
    if (deployUser != null) {
      this.deployUser = deployUser;
    }
  }

  public String getDeployJavaHome() {
    return deployJavaHome;
  }

  public void setDeployJavaHome(@Nullable String deployJavaHome) {
    if (deployJavaHome != null) {
      this.deployJavaHome = deployJavaHome;
    }
  }

  public String getDeployJvmOptions() {
    return deployJvmOptions;
  }

  public void setDeployJvmOptions(@Nullable String deployJvmOptions) {
    if (deployJvmOptions != null) {
      this.deployJvmOptions = deployJvmOptions;
    }
  }

  private String computeFRCAddress(int teamNumber) {
    return "roboRIO-" + teamNumber + "-FRC.local";
  }

  public void setPreferredGeneratedLanguage(String lang) {
    checkArgument(lang.matches("Java|C\\+\\+|Python"),
        "Unsupported language: " + lang);
    this.preferredGeneratedLanguage = lang;
  }

  public String getPreferredGeneratedLanguage() {
    return preferredGeneratedLanguage;
  }

  public void setImplementWpilibPipeline(boolean implementWpilibPipeline) {
    this.implementWpilibPipeline = implementWpilibPipeline;
  }

  public boolean shouldImplementWpilibPipeline() {
    return implementWpilibPipeline;
  }

  public void setGeneratedPipelineName(String generatedPipelineName) {
    checkArgument(generatedPipelineName.matches("^[a-zA-Z]+?[\\w]+$"),
        "Illegal pipeline name: " + generatedPipelineName);
    this.generatedPipelineName = generatedPipelineName;
  }

  public String getGeneratedPipelineName() {
    return generatedPipelineName;
  }

  public void setGeneratedPythonModuleName(String generatedPythonModuleName) {
    checkArgument(generatedPythonModuleName.matches("^[a-z]+[a-z_]+[a-z]+$"),
        "Illegal module name: " + generatedPythonModuleName);
    this.generatedPythonModuleName = generatedPythonModuleName;
  }

  public String getGeneratedPythonModuleName() {
    return generatedPythonModuleName;
  }

  public void setGeneratedJavaPackage(String generatedJavaPackage) {
    checkArgument(generatedJavaPackage.matches("^([a-zA-Z][\\w]*)+(\\.[a-zA-Z][\\w]*)*$"),
        "Illegal package name: " + generatedJavaPackage);
    this.generatedJavaPackage = generatedJavaPackage;
  }

  public String getGeneratedJavaPackage() {
    return generatedJavaPackage;
  }

  public void setCodegenDestDir(File codegenDestDir) {
    checkArgument(codegenDestDir.exists() && codegenDestDir.isDirectory(),
        "Directory does not exist");
    this.codegenDestDir = codegenDestDir;
  }

  public File getCodegenDestDir() {
    return codegenDestDir;
  }

  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("deployAddress", deployAddress)
        .add("deployDir", deployDir)
        .add("deployUser", deployUser)
        .add("deployJavaHome", deployJavaHome)
        .add("deployJvmOptions", deployJvmOptions)
        .add("publishAddress", publishAddress)
        .add("teamNumber", teamNumber)
        .add("generatedPipelineName", generatedPipelineName)
        .add("generatedJavaPackage", generatedJavaPackage)
        .add("generatedPythonModuleName", generatedPythonModuleName)
        .toString();
  }

  @Override
  @SuppressWarnings("PMD.CloneThrowsCloneNotSupportedException")
  public ProjectSettings clone() {
    try {
      return (ProjectSettings) super.clone();
    } catch (CloneNotSupportedException impossible) {
      Throwables.propagate(impossible);
    }

    return null;
  }
}
