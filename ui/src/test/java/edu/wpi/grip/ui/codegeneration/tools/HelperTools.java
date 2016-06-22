package edu.wpi.grip.ui.codegeneration.tools;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.core.CvType;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import static org.junit.Assert.assertFalse;
import org.bytedeco.javacpp.indexer.UByteIndexer;
public class HelperTools {

	/**
	 * Checks if two mats are similar within a given threshold.
	 * @param mat1 first mat
	 * @param mat2 second mat
	 * @param threshold the threshold of difference. 0 threshold is true for only
	 * images that are the same. 10 threshold works pretty well.
	 * @return true if they are similar enough
	 */
	public static boolean equalMatCheck(Mat mat1, Mat mat2, double threshold) {
		if (mat1.cols() != mat2.cols() || mat1.rows() != mat2.rows()) {
			return false;
		}
		Mat tempMat = new Mat();
		Core.absdiff(mat1, mat2, tempMat);
		int matDiff = 0;
		for (int i = 0; i < tempMat.rows(); i++) {
			for (int j = 0; j < tempMat.cols(); j++) {
				double[] pixVal = tempMat.get(i, j);
				double total = 0;
				for (double val : pixVal) {
					total += val * val;
				}
				matDiff += Math.sqrt(total);
			}
		}
		return matDiff <= (tempMat.rows() * tempMat.cols() * threshold);
	}
	/**
	 * Converts a bytedeco Mat to an OpenCV Mat.
	 * @param input the bytedeco Mat to convert
	 * @return an OpenCV Mat
	 */
	public static Mat bytedecoMatToCVMat(org.bytedeco.javacpp.opencv_core.Mat input){
		UByteIndexer idxer= input.createIndexer();
		Mat out = new Mat(idxer.rows(),idxer.cols(),CvType.CV_8UC3);
		for(int row = 0; row<idxer.rows(); row++){
			for(int col = 0; col<idxer.cols(); col++){
				byte data[] = new byte[3];
				for(int channel = 0; channel<idxer.channels(); channel++){
					data[channel] = (byte)(idxer.get(row,col,channel)&0xFF);
				}
				out.put(row, col, data);
			}
		}
		return out;
	}
	
	public static void displayMats(Mat gen, Mat grip){
		JFrame frame = new JFrame();
		frame.setSize(1000, 1000);
		frame.add(createImg("Generated",gen), BorderLayout.WEST);
		frame.add(createImg("Grip",grip), BorderLayout.EAST);
		frame.pack();
		frame.setVisible(true);
		frame.repaint();
		try{
		Thread.sleep(5000);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
	
	private static JLabel createImg(String label, Mat mat){
		MatOfByte matOfBytes = new MatOfByte();
		Imgcodecs.imencode(".jpg", mat, matOfBytes);
		BufferedImage img = null;
		try(ByteArrayInputStream imgStream = new ByteArrayInputStream(matOfBytes.toArray())){
			img = ImageIO.read(imgStream);
		} catch(IOException e){
			e.printStackTrace();
		}
		return new JLabel(label,new ImageIcon(img),0);
		
	}
}
