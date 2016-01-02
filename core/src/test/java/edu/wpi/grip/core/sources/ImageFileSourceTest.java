package edu.wpi.grip.core.sources;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.OutputSocket;
import edu.wpi.grip.util.Files;
import edu.wpi.grip.util.ImageWithData;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertTrue;

/**
 *
 */
public class ImageFileSourceTest {
    private final ImageWithData imageFile = Files.imageFile;
    private final File textFile = Files.textFile;
    private static EventBus eventBus;

    @Before
    public void setUp() throws URISyntaxException {
        this.eventBus = new EventBus();
    }

    @Test
    public void testLoadImageToMat() throws IOException {
        // Given above setup
        // When
        final ImageFileSource fileSource = new ImageFileSource(eventBus, this.imageFile.file);
        fileSource.load();
        OutputSocket<Mat> outputSocket = fileSource.getOutputSockets()[0];

        // Then
        assertTrue("The output socket's value was empty.", outputSocket.getValue().isPresent());

        imageFile.assertSameImage(outputSocket.getValue().get());
    }

    @Test(expected = IOException.class)
    public void testReadInTextFile() throws IOException {
        final ImageFileSource fileSource = new ImageFileSource(eventBus, this.textFile);
        fileSource.load();
        OutputSocket<Mat> outputSocket = fileSource.getOutputSockets()[0];
        assertTrue("No matrix should have been returned.", outputSocket.getValue().get().empty());
    }

    @Test(expected = IOException.class)
    public void testReadInFileWithoutExtension() throws MalformedURLException, IOException {
        final File testFile = new File("temp" + File.separator + "fdkajdl3eaf");

        final ImageFileSource fileSource = new ImageFileSource(eventBus, testFile);
        fileSource.load();
        OutputSocket<Mat> outputSocket = fileSource.getOutputSockets()[0];
        assertTrue("No matrix should have been returned.", outputSocket.getValue().get().empty());
    }

    @Test(expected = IllegalStateException.class)
    public void testNotCallingLoadThrowsIllegalState() {
        final ImageFileSource source = new ImageFileSource(eventBus, this.imageFile.file);
        // Calling this before loading the image should throw an exception
        source.getOutputSockets();
    }
}