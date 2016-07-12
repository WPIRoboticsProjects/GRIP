package edu.wpi.grip.ui.codegeneration.tools;

import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import edu.wpi.grip.ui.codegeneration.JavaTMethods;

import static org.junit.Assert.fail;

public class JavaPipelineInterfacer implements PipelineInterfacer {
  private Class pipeline;
  private Object instance;
  private JavaTMethods tMeth;

  public JavaPipelineInterfacer(String className) {
    tMeth = new JavaTMethods();
    pipeline = PipelineCreator.makeClass(className);
    try {
      instance = pipeline.getConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException
        | NoSuchMethodException | SecurityException e) {
      e.printStackTrace();
      fail("Failure to instantiate class " + className);
    }
  }

  public void setSource(int num, Object value) {
    try {
      pipeline.getMethod("setsource" + num, value.getClass()).invoke(instance, value);
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      e.printStackTrace();
      fail("setsource" + num + " is not valid for class " + value.getClass().getSimpleName() + ""
          + " because there was a: ");
    }
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
      pipeline.getMethod("process").invoke(instance);
    } catch (IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException
        | SecurityException e) {
      e.printStackTrace();
      fail("process method doesn't exist");
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
      e.printStackTrace();
      fail(tMeth.name(name) + " method does not exist");
      return null;
    }
    switch (type) {
      case LINES:
        List<Object> inputLines = (List<Object>) out;
        ArrayList<JavaLine> lines = new ArrayList<JavaLine>(inputLines.size());
        for (int idx = 0; idx < inputLines.size(); idx++) {
          lines.add(idx, new JavaLine(inputLines.get(idx)));
        }
        return lines;
      default:
        return out;
    }
  }

  @Override
  public void setSwitch(String name, boolean value) {
    try {
      pipeline.getMethod("set" + tMeth.name(name), boolean.class).invoke(instance, value);
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      e.printStackTrace();
      fail(name + "is not a valid method");
    }
  }

  @Override
  public void setValve(String name, boolean value) {
    try {
      pipeline.getMethod("set" + tMeth.name(name), boolean.class).invoke(instance, value);
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      e.printStackTrace();
      fail(name + "is not a valid method");
    }
  }

}
