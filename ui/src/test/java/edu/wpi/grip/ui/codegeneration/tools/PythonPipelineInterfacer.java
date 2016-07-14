package edu.wpi.grip.ui.codegeneration.tools;

import edu.wpi.grip.ui.codegeneration.PythonTMethods;

import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PythonPipelineInterfacer implements PipelineInterfacer {
  StringBuilder str;
  BufferedWriter out;
  static File codeDir = null;
  private PythonTMethods tMeth;

  static {
    try {
      codeDir = new File(PipelineGenerator.class.getProtectionDomain()
          .getCodeSource().getLocation().toURI());
    } catch (URISyntaxException e) {
      e.printStackTrace();
      fail("Could not load code directory");
    }
  }

  public PythonPipelineInterfacer(String className) {
    tMeth = new PythonTMethods();
    try {
      Runtime.getRuntime().exec("cd " + codeDir.getAbsolutePath().toString());
    } catch (IOException e) {
      e.printStackTrace();
    }
    str = new StringBuilder();
    str.append("import sys\n");
    str.append("import cv2\n");
    str.append("sys.path.insert(0, \'");
    str.append(codeDir.getAbsolutePath().toString());
    str.append("\')\n");
    str.append("import ");
    str.append(className);
    str.append("\n");
    str.append("pipe = ");
    str.append(className);
    str.append(".");
    str.append(className);
    str.append("()\n");
    try {
      out = new BufferedWriter(new FileWriter("testing.py"));
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void setNumSource(int num, Number value) {
    str.append("source = ").append(value.doubleValue());
    str.append("\n").append("pipe.set_source");
    str.append(num).append("(source)\n");
  }
  
  @Override
  public void setMatSource(int num, File img) {
    str.append("source = cv2.imread(\"");
    str.append(img.getAbsolutePath());
    str.append("\")\n");
    str.append("pipe.set_source");
    str.append(num);
    str.append("(source)\n");
  }

  @Override
  public void process() {
    str.append("pipe.process()\n");
  }

  @Override
  public Object getOutput(String name, GenType type) {
    String pName = tMeth.pyName(name);
    Object objectOut = null;
    try {
      switch (type) {
        case BLOBS:
          objectOut = getOutputBlobs(pName);
          break;
        case NUMBER:
          objectOut = getOutputNum(pName);
          break;
        case BOOLEAN:
          break;
        case POINT:
          objectOut = getOutputPoint(pName);
          break;
        case SIZE:
          objectOut = getOutputSize(pName);
          break;
        case LINES:
          objectOut = getOutputLines(pName);
          break;
        case CONTOURS:
          objectOut = getOutputContours(pName);
          break;
        case LIST:
          break;
        case IMAGE:
          objectOut = getOutputImage(pName);
          break;
        default:
          objectOut = null;
          break;
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    return objectOut;
  }

  private Object getOutputImage(String name) throws IOException {
    StringBuilder temp = new StringBuilder();
    temp.append("mat = pipe.");
    temp.append(name);
    temp.append("\n");
    temp.append("cv2.imwrite(\"img.png\", mat)\n");
    runProcess(temp.toString());

    File img = new File("img.png");
    Mat out = Imgcodecs.imread("img.png", -1);
    return out;
  }

  private Object getOutputPoint(String name) throws IOException {
    StringBuilder temp = new StringBuilder();
    temp.append("print (int(pipe.");
    temp.append(name);
    temp.append("[0]))\n");
    temp.append("print (int(pipe.");
    temp.append(name);
    temp.append("[1]))\n");
    BufferedReader in = runProcess(temp.toString());

    int val1 = new Integer(in.readLine()).intValue();
    int val2 = new Integer(in.readLine()).intValue();
    return new Point(val1, val2);
  }

  private Object getOutputBlobs(String name) throws IOException {
    StringBuilder temp = new StringBuilder();
    temp.append("for blob in pipe.");
    temp.append(name);
    temp.append(":\n");
    temp.append("        print((int)(blob.pt[0]))\n"
        + "        print((int)(blob.pt[1]))\n"
        + "        print((int)(blob.size))\n");

    BufferedReader in = runProcess(temp.toString());

    MatOfKeyPoint blobs = new MatOfKeyPoint();
    String nextIn;
    assertTrue("empty blobs", (nextIn = in.readLine()) != null);
    List<KeyPoint> points = new ArrayList<>();
    while (!(nextIn == null)) {
      points.add(new KeyPoint(Integer.valueOf(nextIn),
          Integer.valueOf(in.readLine()), Integer.valueOf(in.readLine())));
      nextIn = in.readLine();
    }
    blobs.fromList(points);
    return blobs;
  }

  private Object getOutputLines(String name) throws IOException {
    StringBuilder temp = new StringBuilder();
    temp.append("for line in pipe.");
    temp.append(name);
    temp.append(":\n");
    temp.append("        print ((int)(line.x1))\n"
        + "        print ((int)(line.y1))\n"
        + "        print ((int)(line.x2))\n"
        + "        print ((int)(line.y2))\n");

    BufferedReader in = runProcess(temp.toString());

    List<PyLine> lines = new ArrayList<>();
    String nextIn;
    assertTrue("empty Lines", (nextIn = in.readLine()) != null);
    while (!(nextIn == null)) {
      lines.add(new PyLine(Integer.valueOf(nextIn),
          Integer.valueOf(in.readLine()), Integer.valueOf(in.readLine()), Integer.valueOf(in
          .readLine())));
      nextIn = in.readLine();
    }
    return lines;
  }

  private Object getOutputContours(String name) throws IOException {
    StringBuilder temp = new StringBuilder();
    temp.append("for contour in pipe.");
    temp.append(name);
    temp.append(":\n");
    temp.append("        print (\'c\')\n"
        + "        for point in contour:\n"
        + "            print (point[0][0])\n"
        + "            print (point[0][1])\n");
    BufferedReader in = runProcess(temp.toString());

    List<MatOfPoint> contours = new ArrayList();
    String nextIn;
    assertTrue("empty contours", (nextIn = in.readLine()) != null);
    while (nextIn != null) {
      if (nextIn.equals("c")) {
        nextIn = in.readLine();
        List<Point> points = new ArrayList<>();
        MatOfPoint tmpMat = new MatOfPoint();
        while (!(nextIn == null || nextIn.isEmpty() || nextIn.equals("c"))) {
          points.add(new Point(Integer.valueOf(nextIn), Integer.valueOf(in.readLine())));
          nextIn = in.readLine();
        }
        tmpMat.fromList(points);
        contours.add(tmpMat);
      } else {
        nextIn = in.readLine();
      }
    }


    return contours;
  }

  private Object getOutputSize(String name) throws IOException {
    StringBuilder temp = new StringBuilder();
    temp.append("print (int(pipe.");
    temp.append(name);
    temp.append("[0]))\n");
    temp.append("print (int(pipe.");
    temp.append(name);
    temp.append("[1]))\n");
    BufferedReader in = runProcess(temp.toString());
    int val1 = new Integer(in.readLine()).intValue();
    int val2 = new Integer(in.readLine()).intValue();
    return new Size(val1, val2);
  }

  private Object getOutputNum(String name) throws IOException {
    StringBuilder temp = new StringBuilder();
    temp.append("print (pipe.");
    temp.append(name);
    temp.append(")\n");
    BufferedReader in = runProcess(temp.toString());
    String input = in.readLine();
    if (!input.equals("None")) {
      double val = new Double(input).doubleValue();
      return val;
    } else {
      return null;
    }
  }

  private BufferedReader runProcess(String temp) throws IOException {
    out.write(str.toString() + temp);
    out.close();
    ProcessBuilder pb = new ProcessBuilder("python3", "testing.py");
    Process p = pb.start();
    try {
      p.waitFor();
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    }
    BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

    String s = null;

    try {
      out = new BufferedWriter(new FileWriter("testing.py"));
    } catch (IOException e) {
      e.printStackTrace();
    }
    return new BufferedReader(new InputStreamReader(p.getInputStream()));
  }

  @Override
  public void setSwitch(String name, boolean value) {
    str.append("pipe.set");
    str.append(tMeth.pyName(name));
    if (value) {
      str.append("(True)\n");
    } else {
      str.append("(False)\n");
    }
  }

  @Override
  public void setValve(String name, boolean value) {
    str.append("pipe.set");
    str.append(tMeth.pyName(name));
    if (value) {
      str.append("(True)\n");
    } else {
      str.append("(False)\n");
    }
  }

}
