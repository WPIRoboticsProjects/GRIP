package edu.wpi.grip.core.util;

import edu.wpi.grip.util.Files;

import org.bytedeco.javacpp.opencv_core;
import org.junit.Test;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

public class OpenCvShimsTest {

  @Test
  public void testCopyJavaCvToOpenCv() {
    // given
    final opencv_core.Mat javaCvMat = Files.imageFile.createMat();
    final Mat openCvMat = new Mat(1, 1, CvType.CV_8SC1);

    // when
    OpenCvShims.copyJavaCvMatToOpenCvMat(javaCvMat, openCvMat);

    // then

    // test the basic properties (same size, type, etc.)
    assertEquals("Wrong width", javaCvMat.cols(), openCvMat.cols());
    assertEquals("Wrong height", javaCvMat.rows(), openCvMat.rows());
    assertEquals("Wrong type", javaCvMat.type(), openCvMat.type());
    assertEquals("Wrong channel amount", javaCvMat.channels(), openCvMat.channels());
    assertEquals("Wrong bit depth", javaCvMat.depth(), openCvMat.depth());

    // test the raw data bytes - they should be identical
    final int width = javaCvMat.cols();
    final int height = javaCvMat.rows();
    final int channels = javaCvMat.channels();

    final ByteBuffer buffer = javaCvMat.createBuffer();
    assumeThat("JavaCV byte buffer is smaller than expected!",
        buffer.capacity(), greaterThanOrEqualTo(width * height * channels));

    final byte[] javaCvData = new byte[width * height * channels];
    buffer.get(javaCvData);

    final byte[] openCvData = new byte[width * height * channels];
    openCvMat.get(0, 0, openCvData);

    assertArrayEquals("Wrong data bytes", javaCvData, openCvData);
  }

}
