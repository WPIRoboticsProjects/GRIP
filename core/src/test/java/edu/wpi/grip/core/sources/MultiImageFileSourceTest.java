package edu.wpi.grip.core.sources;

import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.sockets.MockOutputSocketFactory;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.util.Files;
import edu.wpi.grip.util.ImageWithData;
import org.bytedeco.javacpp.opencv_core.Mat;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;


public class MultiImageFileSourceTest {
    private static final ImageWithData imageFile = Files.imageFile, gompeiJpegFile = Files.gompeiJpegFile;
    private static final File textFile = Files.textFile;
    private MultiImageFileSource source;
    private MultiImageFileSource sourceWithIndexSet;
    private OutputSocket.Factory osf;

    @Before
    public void setUp() throws IOException {
        osf = new MockOutputSocketFactory(new EventBus());
        source = new MultiImageFileSource(
                new EventBus(),
                osf,
                origin -> null,
                Arrays.asList(imageFile.file, gompeiJpegFile.file));
        sourceWithIndexSet = new MultiImageFileSource(
                new EventBus(),
                osf,
                origin -> null,
                Arrays.asList(imageFile.file, gompeiJpegFile.file), 1);
        source.initialize();
        sourceWithIndexSet.initialize();
    }

    @Test(expected = IOException.class)
    public void createMultiImageFileSourceWithTextFile() throws IOException {
        new MultiImageFileSource(
                new EventBus(),
                osf,
                origin -> null,
                Arrays.asList(imageFile.file, gompeiJpegFile.file, textFile)).initialize();
    }

    @Test
    public void testNextValue() throws Exception {
        source.next();
        OutputSocket<Mat> outputSocket = source.getOutputSockets().get(0);
        source.updateOutputSockets();
        gompeiJpegFile.assertSameImage(outputSocket.getValue().get());
    }

    @Test
    public void testPreviousValue() throws Exception {
        source.previous();

        OutputSocket<Mat> outputSocket = source.getOutputSockets().get(0);
        source.updateOutputSockets();
        gompeiJpegFile.assertSameImage(outputSocket.getValue().get());
    }

    @Test
    public void testConstructedWithIndex() {
        sourceWithIndexSet.updateOutputSockets();
        OutputSocket<Mat> outputSocket = sourceWithIndexSet.getOutputSockets().get(0);
        gompeiJpegFile.assertSameImage(outputSocket.getValue().get());
    }

    @Test
    public void testLoadFromProperties() throws Exception {
        final Properties properties = sourceWithIndexSet.getProperties();
        final MultiImageFileSource newSource = new MultiImageFileSource(
                new EventBus(),
                osf,
                origin -> null,
                properties);
        newSource.initialize();
        newSource.updateOutputSockets();
        OutputSocket<Mat> outputSocket = newSource.getOutputSockets().get(0);
        gompeiJpegFile.assertSameImage(outputSocket.getValue().get());
    }
}
