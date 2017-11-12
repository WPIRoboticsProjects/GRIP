package edu.wpi.grip.core.operations.composite;

import edu.wpi.grip.util.Files;

import org.bytedeco.javacpp.opencv_core;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencv.core.Mat;

import java.lang.reflect.Constructor;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import static edu.wpi.grip.core.operations.composite.PublishVideoOperation.generateStreamUrl;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeThat;

public class PublishVideoOperationTest {

  @BeforeClass
  public static void loadOpenCvJni() {
    // Make sure the OpenCV JNI is loaded
    blackHole(PublishVideoOperation.DESCRIPTION);
  }

  @Test
  public void testGenerateStreams() {
    // given
    final String firstHost = "localhost";
    final int firstAddress = 0x7F_00_00_01;  // loopback 127.0.0.1
    final String secondHost = "driver-station";
    final int secondAddress = 0x0A_01_5A_05; // 10.1.90.5, FRC driver station IP
    final String thirdHost = "network-mask";
    final int thirdAddress = 0xFF_FF_FF_FF;  // 255.255.255.255, not loopback
    final List<NetworkInterface> networkInterfaces =
        Arrays.asList(
            newNetworkInterface(
                "MockNetworkInterface0", 0,
                new InetAddress[]{
                    newInet4Address(firstHost, firstAddress)
                }),
            newNetworkInterface("MockNetworkInterface1", 1,
                new InetAddress[]{
                    newInet4Address(secondHost, secondAddress),
                    newInet4Address(thirdHost, thirdAddress)
                })
        );
    final int port = 54321;

    // when
    final String[] streams = PublishVideoOperation.generateStreams(networkInterfaces, port);

    // then
    assertEquals("Four URLs should have been generated", 4, streams.length);

    // stream URLs should be generated only for non-loopback IPv4 addresses
    assertEquals(generateStreamUrl(secondHost, port), streams[0]);
    assertEquals(generateStreamUrl(formatIpv4Address(secondAddress), port), streams[1]);
    assertEquals(generateStreamUrl(thirdHost, port), streams[2]);
    assertEquals(generateStreamUrl(formatIpv4Address(thirdAddress), port), streams[3]);

  }

  private static String formatIpv4Address(int address) {
    return String.format(
        "%d.%d.%d.%d",
        address >> 24 & 0xFF,
        address >> 16 & 0xFF,
        address >> 8 & 0xFF,
        address & 0xFF
    );
  }

  private static NetworkInterface newNetworkInterface(String name,
                                                      int index,
                                                      InetAddress[] addresses) {
    try {
      Constructor<NetworkInterface> constructor =
          NetworkInterface.class.getDeclaredConstructor(
              String.class,
              int.class,
              InetAddress[].class);
      constructor.setAccessible(true);
      return constructor.newInstance(name, index, addresses);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  private static Inet4Address newInet4Address(String hostname, int address) {
    try {
      Constructor<Inet4Address> constructor =
          Inet4Address.class.getDeclaredConstructor(String.class, int.class);
      constructor.setAccessible(true);
      return constructor.newInstance(hostname, address);
    } catch (ReflectiveOperationException e) {
      throw new AssertionError(e);
    }
  }

  /**
   * Make sure that the JavaCV Mat is compatible with the OpenCV Mat. This should check regressions
   * from having different or otherwise incompatible versions of the OpenCV binaries bundled with
   * JavaCV and the OpenCV library built by WPILib.
   */
  @Test
  public void testCopyJavaCvToOpenCvMatByRawPointer() {
    // Test the GRIP logo
    opencv_core.Mat javaCvMat = Files.imageFile.createMat();

    // when
    Mat openCvMat = new Mat(javaCvMat.address());

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

  // workaround for FindBugs reporting unused variables
  private static void blackHole(Object ignore) {
    // nop
  }

}
