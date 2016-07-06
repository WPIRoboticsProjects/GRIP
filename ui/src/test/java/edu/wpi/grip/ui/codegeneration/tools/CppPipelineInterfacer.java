package edu.wpi.grip.ui.codegeneration.tools;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.UnsupportedOperationException;
import java.net.URISyntaxException;
import java.net.URL;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CppPipelineInterfacer implements PipelineInterfacer {
  private static File codeDir;
  static{
    System.loadLibrary("genJNI");
    codeDir = PipelineGenerator.codeDir.getAbsoluteFile();
  }
  
  public CppPipelineInterfacer(String libName){
    try {
      String libBase = codeDir.getAbsolutePath() + File.separator + libName;
      Process cmake = new ProcessBuilder("cmake","CMakeLists.txt" ,"-DNAME="+libName).directory(codeDir).start();
      String error = runProcess(cmake);
      assertEquals("Failed to cmake " + libName + error, 0, cmake.exitValue());
      Process make = new ProcessBuilder("make").directory(codeDir).start();
      error = runProcess(make);
      assertEquals("Failed to compile " + libName + error, 0, make.exitValue());
    } catch (IOException e) {
      e.printStackTrace();
      fail("Could not compile " + libName + " due to :" + e.getMessage());
    }
    init(codeDir.getAbsolutePath()+"/lib" + libName + ".dylib");
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
        return getMat(num);
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
    throw new UnsupportedOperationException("C++ does not yet support getOutput with type: " + type);
  }

  @Override
  public void setSwitch(int num, boolean value) {
    setCondition(num, value);
  }

  @Override
  public void setValve(int num, boolean value) {
    setCondition(num, value);
  }

  private String runProcess(Process proc) throws IOException{
    waitOn(proc);
    InputStream err = proc.getErrorStream();
    StringBuilder builder = new StringBuilder();
    builder.append(" with error ");
    while(err.available()>0){
      builder.append((char)err.read());
    }
    return builder.toString();
  }
  
  private void waitOn(Process proc){
    try {
      proc.waitFor();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  
  private Mat getMat(int num){
    String matPath = codeDir.toPath().resolve(getMatFile(num)).toFile().getAbsolutePath();
    System.out.println(matPath);
    return Imgcodecs.imread(matPath, -1);
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
