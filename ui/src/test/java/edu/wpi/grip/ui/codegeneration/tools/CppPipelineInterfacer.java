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
    init(codeDir.getAbsolutePath() + "/lib" + libName + ".dylib");
  }

  @Override
  public void setMatSource(int num, File img) {
    setMatSource(num, img.getAbsolutePath());
  }

  @Override
  public void setNumSource(int num, Number val) {
    setNumSource(num, val.doubleValue());
  }

  @Override
  public Object getOutput(String name, GenType type) {
    int num = 0;
    switch (type) {
      case BLOBS:
        MatOfKeyPoint blobs = new MatOfKeyPoint();
        getBlobs(num, blobs.nativeObj);
        return blobs;
      case BOOLEAN:
        return new Boolean(getBoolean(num));
      case CONTOURS:
        int numContours = getNumContours(num);
        ArrayList<MatOfPoint> contours = new ArrayList<MatOfPoint>(numContours);
        long[] addresses = new long[numContours];
        for (int idx = 0; idx < numContours; idx++) {
          contours.add(idx, new MatOfPoint());
          addresses[idx] = contours.get(idx).nativeObj;
        }
        getContours(num, addresses);
        return contours;
      case IMAGE:
        return getMat(num);
      case LINES:
        double[][] linePts = getLines(num);
        List<CppLine> lines = new ArrayList<CppLine>(linePts.length);
        for (int idx = 0; idx < linePts.length; idx++) {
          double[] pts = linePts[idx];
          lines.add(idx, new CppLine(pts[0], pts[1], pts[2], pts[3], pts[4], pts[5]));
        }
        return lines;
      case LIST:
        break;
      case NUMBER:
        return new Double(getDouble(num));
      case POINT:
        double[] pnt = getSizeOrPoint(num, false);
        return new Point(pnt[0], pnt[1]);
      case SIZE:
        double[] sz = getSizeOrPoint(num, true);
        return new Size(sz[0], sz[1]);
      default:
        break;

    }
    throw new UnsupportedOperationException("C++ does not yet support getOutput with type: "
        + type);
  }

  @Override
  public void setSwitch(String name, boolean value) {
    int num = 0;
    setCondition(num, value);
  }

  @Override
  public void setValve(String name, boolean value) {
    int num = 0;
    setCondition(num, value);
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

  private Mat getMat(int num) {
    Mat out = new Mat();
    getMatNative(num, out.nativeObj);
    return out;
  }
  
  @Override
  public native void process();
  
  private native void setMatSource(int num, String path);
  
  private native void setNumSource(int num, double value);
  
  private native void getMatNative(int num, long addr);

  private native double getDouble(int num);

  private native boolean getBoolean(int num);

  private native void setCondition(int num, boolean value);

  private native void init(String libName);

  private native void dispose();

  private native double[] getSizeOrPoint(int num, boolean size);

  private native void getBlobs(int num, long retAddr);

  private native int getNumContours(int num);

  /**
   * Gets the contours from specified output.
   *
   * @param num   the output number.
   * @param addrs an array of nativeAddresses of MatOfPoint objects. Note the size of addrs should
   *              be the number returned from getNumContours.
   */
  private native void getContours(int num, long[] addrs);

  private native double[][] getLines(int num);

  private long nativeHandle;

}
