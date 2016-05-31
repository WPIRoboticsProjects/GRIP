
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class CameraTest{
	
	private static JFrame jFrame = new JFrame();

	public static void main(String[] args){
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

    VideoCapture camera = new VideoCapture(0);
    try {
		Thread.sleep(1000);
	} catch (InterruptedException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    
    Pipeline pipe = new Pipeline();
    
	jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
	while(true){
		Mat frame = new Mat();
    	camera.read(frame);
    	pipe.setsource0(frame);
    	pipe.setsource1(frame);
    	pipe.processImage();
    	showFrame(pipe.getoutput1());
	}

    }




	public static void showFrame(Mat img) {
	MatOfByte matOfByte = new MatOfByte();
	Imgcodecs.imencode(".jpg", img, matOfByte);
	byte[] byteArray = matOfByte.toArray();
	BufferedImage bufImage = null;
	try {
		InputStream in = new ByteArrayInputStream(byteArray);
		bufImage = ImageIO.read(in);
		jFrame.setContentPane(new JLabel(new ImageIcon(bufImage)));
		jFrame.pack();
		jFrame.setVisible(true);
		jFrame.repaint();
	} catch (Exception e) {
		e.printStackTrace();
	}
	}
}

