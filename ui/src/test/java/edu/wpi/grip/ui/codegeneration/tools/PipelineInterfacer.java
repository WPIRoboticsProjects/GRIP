package edu.wpi.grip.ui.codegeneration.tools;

import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.fail;

public class PipelineInterfacer {
  private Class pipeline;
  private Object instance;

  public PipelineInterfacer(String className) {
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

  public void setSourceAsObject(int num, Object value) {
    try {
      pipeline.getMethod("setsource" + num, Object.class).invoke(instance, value);
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      e.printStackTrace();
      fail("setsource" + num + " is not valid for class " + value.getClass().getSimpleName() + ""
          + " because there was a: ");
    }
  }

  public void setMatSource(int num, File img) {
    setSource(num, Imgcodecs.imread(img.getAbsolutePath()));
  }

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

  public Object getOutput(int num) {
    try {
      return pipeline.getMethod("getoutput" + num).invoke(instance);
    } catch (IllegalAccessException | IllegalArgumentException
        | InvocationTargetException | NoSuchMethodException
        | SecurityException e) {
      e.printStackTrace();
      fail("getoutput" + num + " method does not exist");
      return null;
    }
  }

  public void setSwitch(int num, boolean value) {
    try {
      pipeline.getMethod("setSwitch" + num, boolean.class).invoke(instance, value);
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      e.printStackTrace();
      fail("setSwitch" + num + "is not a valid method");
    }
  }

  public void setValve(int num, boolean value) {
    try {
      pipeline.getMethod("setValve" + num, boolean.class).invoke(instance, value);
    } catch (NoSuchMethodException | SecurityException | IllegalAccessException
        | IllegalArgumentException | InvocationTargetException e) {
      e.printStackTrace();
      fail("setValve" + num + "is not a valid method");
    }
  }

}
