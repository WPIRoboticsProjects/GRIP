package edu.wpi.grip.core.sources;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.OutputSocket;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.junit.Assert.*;

/**
 *
 */
public class ImageFileSourceTest {
    private File imageFile;
    private File textFile;
    private static EventBus eventBus;

    @Before
    public void setUp() throws URISyntaxException {
        this.eventBus = new EventBus();
        textFile = new File(ImageFileSourceTest.class.getResource("/edu/wpi/grip/images/NotAnImage.txt").toURI());
        imageFile = new File(ImageFileSourceTest.class.getResource("/edu/wpi/grip/images/GRIP_Logo.png").toURI());
    }

    @Test
    public void testLoadImageToMat() throws IOException {
        // Given above setup
        // When
        final ImageFileSource fileSource = new ImageFileSource(eventBus, this.imageFile);
        OutputSocket<Mat> outputSocket = fileSource.getOutputSockets()[0];

        // Then
        assertNotNull("The output socket's value was null.", outputSocket.getValue());

        // Check that the image that is read in is 2 dimentional
        assertEquals("Matrix from loaded image did not have expected number of rows.", 183, outputSocket.getValue().rows());
        assertEquals("Matrix from loaded image did not have expected number of cols.", 480, outputSocket.getValue().cols());
    }

    @Test(expected = IOException.class)
    public void testReadInTextFile() throws IOException {
        final ImageFileSource fileSource = new ImageFileSource(eventBus, this.textFile);
        OutputSocket<Mat> outputSocket = fileSource.getOutputSockets()[0];
        assertTrue("No matrix should have been returned.", outputSocket.getValue().empty());
    }

    @Test(expected = IOException.class)
    public void testReadInFileWithoutExtension() throws MalformedURLException, IOException {
        final File testFile = new File("temp" + File.separator + "fdkajdl3eaf");

        final ImageFileSource fileSource = new ImageFileSource(eventBus, testFile);
        OutputSocket<Mat> outputSocket = fileSource.getOutputSockets()[0];
        assertTrue("No matrix should have been returned.", outputSocket.getValue().empty());
    }
}