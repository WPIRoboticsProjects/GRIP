package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.util.Files;

import org.bytedeco.javacpp.opencv_core;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class PublishVideoOperationTest {

  @BeforeClass
  public static void initialize() {
    // Make sure the OpenCV JNI is loaded
    blackHole(PublishVideoOperation.DESCRIPTION);
  }

  @Test
  public void testCopyJavaCvToOpenCvMat() {
    // given
    final Mat openCvMat = new Mat();

    // then (with the GRIP logo)
    test(Files.imageFile.createMat(), openCvMat);

    // and again (with gompei) to confirm that changing the input will be cleanly copied to the
    // output image and cleanly overwrite any existing data
    test(Files.gompeiJpegFile.createMat(), openCvMat);
  }

  private static void test(opencv_core.Mat javaCvMat, Mat openCvMat) {
    // when
    PublishVideoOperation.copyJavaCvToOpenCvMat(javaCvMat, openCvMat);

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
    assertThat("JavaCV byte buffer is smaller than expected!",
        buffer.capacity(), greaterThanOrEqualTo(width * height * channels));

    final byte[] javaCvData = new byte[width * height * channels];
    buffer.get(javaCvData);

    final byte[] openCvData = new byte[width * height * channels];
    openCvMat.get(0, 0, openCvData);

    assertArrayEquals("Wrong data bytes", javaCvData, openCvData);
  }

  // workaround for FindBugs reporting unused variables
  private static void blackHole(Object ignore) {
    // nop
  }

}
