package edu.wpi.grip.core.sources;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.Socket;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.*;

/**
 *
 */
public class ImageFileSourceTest {
    private final URL imageUrl = ImageFileSourceTest.class.getResource("/edu/wpi/grip/images/GRIP_Logo.png");
    private final URL textUrl = ImageFileSourceTest.class.getResource("/edu/wpi/grip/images/NotAnImage.txt");
    private static EventBus eventBus;
    private static ImageFileSource fileSource;

    @Before
    public void setUp(){
        this.eventBus = new EventBus();
        this.fileSource = new ImageFileSource(eventBus);
    }

    @Test
    public void testLoadImageToMat(){
        // Given above setup
        // When
        fileSource.loadImage(imageUrl);
        Socket<Mat> outputSocket = fileSource.getOutputSockets()[0];

        // Then
        assertNotNull("The output socket's value was null.", outputSocket.getValue());

        // Check that the image that is read in is 2 dimentional
        assertEquals("Matrix from loaded image did not have expected number of rows.", 183, outputSocket.getValue().rows());
        assertEquals("Matrix from loaded image did not have expected number of cols.", 480, outputSocket.getValue().cols());
    }

    @Test
    public void testReadInTextFile(){
        fileSource.loadImage(textUrl);
        Socket<Mat> outputSocket = fileSource.getOutputSockets()[0];
        assertTrue("No matrix should have been returned.", outputSocket.getValue().empty());
    }

    @Test
    public void testReadInFileWithoutExtention() throws MalformedURLException {
        fileSource.loadImage(new URL("file://temp/fdkajdl3eaf"));
        Socket<Mat> outputSocket = fileSource.getOutputSockets()[0];
        assertTrue("No matrix should have been returned.", outputSocket.getValue().empty());
    }
}