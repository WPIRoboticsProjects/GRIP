package edu.wpi.grip.ui.codegeneration.tools;

import edu.wpi.grip.core.sockets.InputSocket;

import org.bytedeco.javacpp.indexer.UByteIndexer;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class HelperTools {
  private static final Logger logger = Logger.getLogger(HelperTools.class.getName());

  /**
   * Calculates the average per pixel difference between two Mats. If two Mats are perfectly equal
   * this will be 0. Less than 10 is a good value for similar images.
   *
   * @param mat1 one of the two Mats.
   * @param mat2 the other Mat.
   * @return the average difference.
   */
  public static double matAvgDiff(Mat mat1, Mat mat2) {
    assertFalse("genMat is empty", mat1.empty());
    assertFalse("gripMat is empty", mat2.empty());
    assertTrue("Mat size is not equal. gencols: " + mat1.cols() + " gripcols: " + mat2.cols()
        + " genrows: " + mat1.rows() + " griprows: " + mat2.rows(), mat1
        .cols() == mat2.cols() && mat1.rows() == mat2.rows());
    assertTrue("Mats have different number of channels", mat1.channels() == mat2.channels());
    assertTrue("Mats have different Types. Mat1 is type: " + mat1.type() + " Mat 2 is type: "
        + mat2.type(), mat1.type() == mat2.type());
    Mat diff = new Mat();
    Core.absdiff(mat1, mat2, diff); //Take absolute difference between two mats
    double matDiff = 0;
    for (int row = 0; row < diff.rows(); row++) {
      for (int col = 0; col < diff.cols(); col++) {
        double[] pixVal = diff.get(row, col); //per channel values of given pixel
        double total = 0;
        for (double val : pixVal) {
          total += val * val; //convert to a magnitude squared
        }
        matDiff += Math.sqrt(total); //convert to absolute magnitude
      }
    }
    return matDiff / (diff.rows() * diff.cols()); //divide by number of pixels
  }

  /**
   * Converts a bytedeco Mat to an OpenCV Mat.
   *
   * @param input the bytedeco Mat to convert
   * @return an OpenCV Mat
   */
  public static Mat bytedecoMatToCVMat(org.bytedeco.javacpp.opencv_core.Mat input) {
    UByteIndexer idxer = input.createIndexer();
    Mat out = new Mat(idxer.rows(), idxer.cols(), CvType.CV_8UC(idxer.channels()));
    //Mat out = new Mat(idxer.rows(),idxer.cols(),input.type());
    for (int row = 0; row < idxer.rows(); row++) {
      for (int col = 0; col < idxer.cols(); col++) {
        byte[] data = new byte[3];
        for (int channel = 0; channel < idxer.channels(); channel++) {
          data[channel] = (byte) (idxer.get(row, col, channel) & 0xFF);
        }
        out.put(row, col, data);
      }
    }
    return out;
  }

  public static void setEnumSocket(InputSocket sock, String id) {
    Object[] options = (Object[]) sock.getSocketHint().getDomain().get();
    for (Object option : options) {
      if (option.toString().equals(id)) {
        sock.setValue(option);
        return;
      }
    }
  }


  public static void displayMats(Mat gen, Mat grip) {
    JFrame frame = new JFrame();
    frame.setSize(1000, 1000);
    frame.add(createImg("Generated", gen), BorderLayout.WEST);
    frame.add(createImg("GRIP", grip), BorderLayout.EAST);
    frame.pack();
    frame.setVisible(true);
    frame.repaint();
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
    frame.dispose();
  }

  private static JLabel createImg(String label, Mat mat) {
    MatOfByte matOfBytes = new MatOfByte();
    Imgcodecs.imencode(".jpg", mat, matOfBytes);
    try (ByteArrayInputStream imgStream = new ByteArrayInputStream(matOfBytes.toArray())) {
      BufferedImage img = ImageIO.read(imgStream);
      return new JLabel(label, new ImageIcon(img), 0);
    } catch (IOException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
      return null;
    }
  }

}
