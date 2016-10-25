package edu.wpi.grip.ui.codegeneration.tools;

import org.opencv.core.Core;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import static org.junit.Assert.fail;

public class PipelineCreator {
  private static final Logger logger = Logger.getLogger(PipelineCreator.class.getName());

  static {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

  private static void compile(String fileName) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
    try {
      fileManager.setLocation(StandardLocation.CLASS_OUTPUT,
          Arrays.asList(PipelineGenerator.getCodeDir()));
    } catch (IOException e1) {
      fail("FileManager could not set output location " + errorBase(fileName));
      logger.log(Level.WARNING, e1.getMessage(), e1);
    }
    compiler.getTask(null, fileManager, null, null, null, fileManager.getJavaFileObjects(
        PipelineGenerator.getCodeDir().toPath().resolve(fileName).toFile())).call();
    try {
      fileManager.close();
    } catch (IOException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }

  private static Class load(String fileName) {
    try {
      if (fileName.endsWith(".py")) {
        return Class.forName(fileName.replace(".py", ""));
      } else if (fileName.endsWith(".java")) {
        return Class.forName(fileName.replace(".java", ""));
      }
    } catch (ClassNotFoundException e) {
      fail("Unable to load class " + errorBase(fileName));
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    return null;
  }

  public static Class makeClass(String fileName) {
    compile(fileName);
    return load(fileName);
  }

  private static String errorBase(String fileName) {
    return "for " + fileName;
  }

  public static void cleanClasses() {
    try {
      File[] files = PipelineGenerator.getCodeDir().toPath().toFile().listFiles((file, name) -> name
          .contains(".class") || name.contains(".java"));
      if (files != null) {
        for (File file : files) {
          file.delete();
        }
      }
    } catch (SecurityException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }
}
