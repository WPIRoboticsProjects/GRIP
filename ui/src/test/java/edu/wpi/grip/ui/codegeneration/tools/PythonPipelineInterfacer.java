package edu.wpi.grip.ui.codegeneration.tools;

import edu.wpi.grip.ui.codegeneration.PythonTMethods;

import org.apache.commons.lang.StringUtils;
import org.opencv.core.KeyPoint;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PythonPipelineInterfacer implements PipelineInterfacer {

  private final StringBuilder str; //NOPMD
  private BufferedWriter out;
  static File codeDir = null;
  private final PythonTMethods tMeth;
  static String pythonCmd;
  static final String newLine = System.lineSeparator();
  private static final String outputFile = "output.txt";
  private static final Logger logger = Logger.getLogger(PythonPipelineInterfacer.class.getName());

  private final List<String> sourceNames = new ArrayList<>();

  static {
    try {
      codeDir = new File(
          PipelineGenerator.class.getProtectionDomain().getCodeSource().getLocation().toURI());
    } catch (URISyntaxException e) {
      fail("Could not load code directory");
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
      pythonCmd = "python";
    } else {
      pythonCmd = "python3";
    }
  }

  public PythonPipelineInterfacer(String className) {
    tMeth = new PythonTMethods();
    str = new StringBuilder();
    str.append("import sys").append(newLine).append("import cv2").append(newLine)
        .append("import os").append(newLine).append("sys.path.insert(0, ")
        .append(pyPath(codeDir.getAbsolutePath())).append(')').append(newLine)
        .append("import ").append(className).append(newLine)
        .append("pipe = ").append(className).append('.')
        .append(className).append("()").append(newLine);
    try {
      out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("testing.py"), "UTF-8"));
    } catch (IOException e) {
      fail("Could not write the testing python file");
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }

  @Override
  public void setNumSource(int num, Number value) {
    sourceNames.add("source" + num);
    str.append("source").append(num).append(" = ").append(value.doubleValue())
        .append(newLine);
  }

  @Override
  public void setMatSource(int num, File img) {
    sourceNames.add("source" + num);
    str.append("source").append(num)
        .append(" = cv2.imread(").append(pyPath(img.getAbsolutePath())).append(')')
        .append(newLine);
  }

  @Override
  public void process() {
    str.append("pipe.process(");
    str.append(StringUtils.join(sourceNames, ", "));
    str.append(')');
    str.append(newLine);
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
      fail("Could not get Output " + name + " with type " + type);
      logger.log(Level.WARNING, e.getMessage(), e);
    }

    return objectOut;
  }

  private Object getOutputImage(String name) throws IOException {
    StringBuilder temp = new StringBuilder(40);
    temp.append("mat = pipe.").append(name).append(newLine).append("cv2.imwrite(\"img.png\", mat)")
        .append(newLine);
    runProcess(temp.toString());

    File img = new File("img.png");
    if (!img.exists()) {
      fail("Output image does not exist!");
    }
    return Imgcodecs.imread("img.png", -1);
  }

  private Object getOutputPoint(String name) throws IOException {
    StringBuilder temp = new StringBuilder(42);
    temp.append("print (int(pipe.").append(name).append("[0]))").append(newLine)
        .append("print (int(pipe.").append(name).append("[1]))").append(newLine);
    BufferedReader in = runProcess(temp.toString());
    int val1 = Integer.parseInt(in.readLine());
    int val2 = Integer.parseInt(in.readLine());
    return new Point(val1, val2);
  }

  private Object getOutputBlobs(String name) throws IOException {
    StringBuilder temp = new StringBuilder(113);
    temp.append("for blob in pipe.").append(name).append(':').append(newLine)
        .append("        print((int)(blob.pt[0]))").append(newLine)
        .append("        print((int)(blob.pt[1]))").append(newLine)
        .append("        print((int)(blob.size))").append(newLine);
    BufferedReader in = runProcess(temp.toString());
    MatOfKeyPoint blobs = new MatOfKeyPoint();
    String nextIn;
    assertTrue("empty blobs", (nextIn = in.readLine()) != null);
    List<KeyPoint> points = new ArrayList<>();
    while (nextIn != null) {
      points.add(new KeyPoint(Integer.parseInt(nextIn), Integer.parseInt(in.readLine()),
          Integer.parseInt(in.readLine())));
      nextIn = in.readLine();
    }
    blobs.fromList(points);
    return blobs;
  }

  private Object getOutputLines(String name) throws IOException {
    StringBuilder temp = new StringBuilder(138);
    temp.append("for line in pipe.").append(name).append(':').append(newLine)
        .append("        print ((int)(line.x1))").append(newLine)
        .append("        print ((int)(line.y1))").append(newLine)
        .append("        print ((int)(line.x2))").append(newLine)
        .append("        print ((int)(line.y2))").append(newLine);
    BufferedReader in = runProcess(temp.toString());
    List<PyLine> lines = new ArrayList<>();
    String nextIn;
    assertTrue("empty Lines", (nextIn = in.readLine()) != null);
    while (nextIn != null) {
      lines.add(new PyLine(Integer.parseInt(nextIn), Integer.parseInt(in.readLine()),
          Integer.parseInt(in.readLine()), Integer.parseInt(in.readLine())));
      nextIn = in.readLine();
    }
    return lines;
  }

  private Object getOutputContours(String name) throws IOException {
    StringBuilder temp = new StringBuilder(133);
    temp.append("for contour in pipe.").append(name).append(':').append(newLine)
        .append("        print (\'c\')").append(newLine).append("        for point in contour:")
        .append(newLine).append("            print (point[0][0])").append(newLine)
        .append("            print (point[0][1])").append(newLine);
    BufferedReader in = runProcess(temp.toString());
    List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
    String nextIn = in.readLine();
    assertTrue("empty contours", nextIn != null);
    List<Point> points = new ArrayList<>();
    while (nextIn != null) {
      if ("c".equals(nextIn)) {
        if (!points.isEmpty()) {
          MatOfPoint tmpMat = new MatOfPoint();
          tmpMat.fromList(points);
          contours.add(tmpMat);
          points.clear();
        }
      } else {
        int x = Integer.parseInt(nextIn);
        int y = Integer.parseInt(in.readLine());
        points.add(new Point(x, y));
      }
      nextIn = in.readLine();
    }
    MatOfPoint tmpMat = new MatOfPoint();
    tmpMat.fromList(points);
    contours.add(tmpMat);
    points.clear();
    return contours;
  }

  private Object getOutputSize(String name) throws IOException {
    StringBuilder temp = new StringBuilder(42);
    temp.append("print (int(pipe.").append(name).append("[0]))").append(newLine)
        .append("print (int(pipe.").append(name).append("[1]))").append(newLine);
    BufferedReader in = runProcess(temp.toString());
    int val1 = Integer.parseInt(in.readLine());
    int val2 = Integer.parseInt(in.readLine());
    return new Size(val1, val2);
  }

  private Object getOutputNum(String name) throws IOException {
    StringBuilder temp = new StringBuilder();
    temp.append("print (pipe.").append(name).append(')').append(newLine);
    BufferedReader in = runProcess(temp.toString());
    final String input = in.readLine();
    if (input != null && !input.equals("None")) {
      return Double.parseDouble(input);
    } else {
      return null;
    }
  }

  private BufferedReader runProcess(String temp) throws IOException {
    out.write(str.toString() + temp);
    out.close();
    ProcessBuilder pb = new ProcessBuilder(pythonCmd, "testing.py");
    pb.redirectOutput(new File(outputFile));
    Process p = pb.start();
    try {
      int exitCode = p.waitFor();
      if (exitCode != 0) {
        InputStream stdErr = p.getErrorStream();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stdErr, "UTF-8"))) {
          StringBuilder errStr = new StringBuilder();
          while (stdErr.available() > 0) {
            errStr.append(reader.readLine());
          }
          fail("Process failed with nonZero exit code " + exitCode + " and error" + newLine
              + errStr.toString());
        }
      }
    } catch (InterruptedException e1) {
      logger.log(Level.WARNING, e1.getMessage(), e1);
    }
    try {
      out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("testing.py"), "UTF-8"));
    } catch (IOException e) {
      fail("Could not run Process");
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    return new BufferedReader(new InputStreamReader(
        new FileInputStream(new File(outputFile)), "UTF-8"));
  }

  @Override
  public void setSwitch(String name, boolean value) {
    str.append("pipe.set").append(tMeth.pyName(name));
    if (value) {
      str.append("(True)").append(newLine);
    } else {
      str.append("(False)").append(newLine);
    }
  }

  @Override
  public void setValve(String name, boolean value) {
    str.append("pipe.set");
    str.append(tMeth.pyName(name));
    if (value) {
      str.append("(True)").append(newLine);
    } else {
      str.append("(False)").append(newLine);
    }
  }

  private String pyPath(String osPath) {
    StringBuilder pathBld = new StringBuilder(22);
    pathBld.append("os.path.normpath(\'")
        .append(osPath.replaceAll(Matcher.quoteReplacement("\\"), "/")).append("\')");
    return pathBld.toString();
  }
}
