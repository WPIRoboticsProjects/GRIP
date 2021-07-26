package edu.wpi.grip.ui.codegeneration.tools;

import edu.wpi.grip.ui.codegeneration.JavaTMethods;

import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.junit.Assert.fail;

public class JavaPipelineInterfacer implements PipelineInterfacer {
  private final Class<?> pipeline;
  private Object instance;
  private final JavaTMethods tMeth;
  private static final Logger logger = Logger.getLogger(JavaPipelineInterfacer.class.getName());

  private final Map<String, Class<?>> sourceTypeMap = new LinkedHashMap<>();
  private final Map<String, Object> sourceValueMap = new LinkedHashMap<>();

  public JavaPipelineInterfacer(String className) {
    tMeth = new JavaTMethods();
    pipeline = PipelineCreator.makeClass(className);
    try {
      instance = pipeline.getConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException e) {
      fail("Failure to instantiate class " + className);
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }

  public void setSource(int num, Object value) {
    sourceTypeMap.put("source" + num, value.getClass());
    sourceValueMap.put("source" + num, value);
  }

  @Override
  public void setMatSource(int num, File img) {
    setSource(num, Imgcodecs.imread(img.getAbsolutePath()));
  }

  @Override
  public void setNumSource(int num, Number value) {
    setSource(num, value);
  }

  @Override
  public void process() {
    try {
      pipeline.getMethod("process", sourceTypeMap.values().toArray(new Class[0]))
          .invoke(instance, sourceValueMap.values().toArray());
    } catch (IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException
        | SecurityException e) {
      fail("Failed to call process with exception: " + e.toString());
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }

  @Override
  public Object getOutput(String name, GenType type) {
    Object out = null;
    try {
      out = pipeline.getMethod(tMeth.name(name)).invoke(instance);
    } catch (IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException
        | SecurityException e) {
      fail(tMeth.name(name) + " method does not exist");
      logger.log(Level.WARNING, e.getMessage(), e);
      return null;
    }
    if (type.equals(GenType.LINES)) {
      List<Object> inputLines = (List<Object>) out;
      ArrayList<JavaLine> lines = new ArrayList<JavaLine>(inputLines.size());
      for (int idx = 0; idx < inputLines.size(); idx++) {
        lines.add(idx, new JavaLine(inputLines.get(idx)));
      }
      return lines;
    } else {
      return out;
    }
  }

  @Override
  public void setSwitch(String name, boolean value) {
    try {
      pipeline.getMethod("set" + tMeth.name(name), boolean.class).invoke(instance, value);
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      fail(name + "is not a valid method");
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }

  @Override
  public void setValve(String name, boolean value) {
    try {
      pipeline.getMethod("set" + tMeth.name(name), boolean.class).invoke(instance, value);
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      fail(name + "is not a valid method");
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }

}
