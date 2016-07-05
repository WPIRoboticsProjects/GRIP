package edu.wpi.grip.ui.codegeneration.tools;

import java.io.File;
import java.lang.UnsupportedOperationException;

import org.opencv.core.Mat;

public class CppPipelineInterfacer implements PipelineInterfacer {
  
  static{
    System.loadLibrary("genJNI");
  }
  
  public CppPipelineInterfacer(String libName){
    init(libName);
  }
  
  @Override
  public void setSource(int num, Object value) {
    throw new UnsupportedOperationException(
        "This shouldn't be called, setSource is not supported yet!");
  }

  @Override
  public void setSourceAsObject(int num, Object value) {
    throw new UnsupportedOperationException(
        "This shouldn't be called, setSourceAsObject is not supported yet!");
  }

  @Override
  public void setMatSource(int num, File img) {
    setMatSource(num, img.getAbsolutePath());
  }

  @Override
  public native void process();

  @Override
  public Object getOutput(int num, GenType type) {
    switch(type){
      case BLOBS:
        break;
      case BOOLEAN:
        return new Boolean(getBoolean(num));
      case CONTOURS:
        break;
      case IMAGE:
        return getMatFile(num);
      case LINES:
        break;
      case LIST:
        break;
      case NUMBER:
        return new Double(getDouble(num));
      case POINT:
        break;
      case SIZE:
        break;
      default:
        break;
      
    }
    return null;
  }

  @Override
  public void setSwitch(int num, boolean value) {
    setCondition(num, value);
  }

  @Override
  public void setValve(int num, boolean value) {
    setCondition(num, value);
  }
  private native String getMatFile(int num);
  private native double getDouble(int num);
  private native boolean getBoolean(int num);
  private native void setCondition(int num, boolean value);
  private native void setMatSource(int num, String path);
  private native void init(String libName);
  private native void dispose();
  private long nativeHandle;
}
