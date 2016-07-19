package edu.wpi.grip.ui.codegeneration.tools;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class CppPipelineInterfacer implements PipelineInterfacer {
  private static File codeDir;

  static {
    System.loadLibrary("genJNI");
    codeDir = PipelineGenerator.codeDir.getAbsoluteFile();
  }

  public CppPipelineInterfacer(String libName) {
    try {
      String libBase = codeDir.getAbsolutePath() + File.separator + libName;
      Process cmake = new ProcessBuilder("cmake", "CMakeLists.txt", "-DNAME="
          + libName).directory(codeDir).start();
      String error = runProcess(cmake);
      assertEquals("Failed to cmake " + libName + error, 0, cmake.exitValue());
      Process make = new ProcessBuilder("make").directory(codeDir).start();
      error = runProcess(make);
      assertEquals("Failed to compile " + libName + error, 0, make.exitValue());
    } catch (IOException e) {
      e.printStackTrace();
      fail("Could not compile " + libName + " due to :" + e.getMessage());
    }
    String platformLibName = System.mapLibraryName(libName);
    init(codeDir.getAbsolutePath() + File.separator + platformLibName);
  }


  @Override
  public Object getOutput(String name, GenType type) {
    name = name.toLowerCase().replaceAll("_", "");
    switch (type) {
      case BLOBS:
        MatOfKeyPoint blobs = new MatOfKeyPoint();
        getBlobs(name, blobs.nativeObj);
        return blobs;
      case BOOLEAN:
        return new Boolean(getBoolean(name));
      case CONTOURS:
        int numContours = getNumContours(name);
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>(numContours);
        long[] addresses = new long[numContours];
        for (int idx = 0; idx < numContours; idx++) {
          contours.add(idx, new MatOfPoint());
          addresses[idx] = contours.get(idx).nativeObj;
        }
        getContours(name, addresses);
        return contours;
      case IMAGE:
        return getMat(name);
      case LINES:
        double[][] linePts = getLines(name);
        List<CppLine> lines = new ArrayList<CppLine>(linePts.length);
        for (int idx = 0; idx < linePts.length; idx++) {
          double[] pts = linePts[idx];
          lines.add(idx, new CppLine(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5]));
        }
        return lines;
      case LIST:
        break;
      case NUMBER:
        return new Double(getDouble(name));
      case POINT:
        double[] pnt = getSizeOrPoint(name, false);
        return new Point(pnt[0], pnt[1]);
      case SIZE:
        double[] sz = getSizeOrPoint(name, true);
        return new Size(sz[0], sz[1]);
      default:
        break;

    }
    throw new UnsupportedOperationException("C++ does not yet support getOutput with type: "
        + type);
  }

  @Override
  public void setSwitch(String name, boolean value) {
    setCondition(name.toLowerCase().replaceAll("_", ""), value);
  }

  @Override
  public void setValve(String name, boolean value) {
    setCondition(name.toLowerCase().replaceAll("_", ""), value);
  }

  private String runProcess(Process proc) throws IOException {
    waitOn(proc);
    InputStream err = proc.getErrorStream();
    StringBuilder builder = new StringBuilder();
    builder.append(" with error\n");
    while (err.available() > 0) {
      builder.append((char) err.read());
    }
    return builder.toString();
  }

  private void waitOn(Process proc) {
    try {
      proc.waitFor();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

  private Mat getMat(String name) {
    Mat out = new Mat();
    getMatNative(name, out.nativeObj);
    return out;
  }
  
  @Override
  public void setMatSource(int num, File img) {
    setMatSource(num, img.getAbsolutePath());
  }

  private native void setMatSource(int num, String path);
  
  @Override
  public void setNumSource(int num, Number val) {
    setNumSource(num, val.doubleValue());
  }
  
  private native void setNumSource(int num, double value);
  
  @Override
  public native void process();
  
  private native void getMatNative(String name, long addr);

  private native double getDouble(String name);

  private native boolean getBoolean(String name);

  private native void setCondition(String name, boolean value);

  private native void init(String libName);

  private native void dispose();

  private native double[] getSizeOrPoint(String name, boolean size);

  private native void getBlobs(String name, long retAddr);

  private native int getNumContours(String name);

  /**
   * Gets the contours from specified output.
   *
   * @param num   the output number.
   * @param addrs an array of nativeAddresses of MatOfPoint objects. Note the size of addrs should
   *              be the number returned from getNumContours.
   */
  private native void getContours(String name, long[] addrs);

  private native double[][] getLines(String name);

  private long nativeHandle;

}
