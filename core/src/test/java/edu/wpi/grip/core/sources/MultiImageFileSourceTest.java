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
import java.util.Arrays;
import java.util.Properties;


public class MultiImageFileSourceTest {
    private static final ImageWithData imageFile = Files.imageFile, gompeiJpegFile = Files.gompeiJpegFile;
    private static final File textFile = Files.textFile;
    private MultiImageFileSource source;
    private MultiImageFileSource sourceWithIndexSet;

    @Before
    public void setUp() throws IOException {
        source = new MultiImageFileSource(
                new EventBus(),
                Arrays.asList(imageFile.file, gompeiJpegFile.file));
        sourceWithIndexSet = new MultiImageFileSource(
                new EventBus(),
                Arrays.asList(imageFile.file, gompeiJpegFile.file), 1);
    }

    @Test(expected = IOException.class)
    public void createMultiImageFileSourceWithTextFile() throws IOException {
        new MultiImageFileSource(new EventBus(), Arrays.asList(imageFile.file, gompeiJpegFile.file, textFile));
    }

    @Test
    public void testNextValue() throws Exception {
        source.nextValue();
        OutputSocket<Mat> outputSocket = source.getOutputSockets()[0];
        gompeiJpegFile.assertSameImage(outputSocket.getValue().get());
    }

    @Test
    public void testPreviousValue() throws Exception {
        source.previousValue();
        OutputSocket<Mat> outputSocket = source.getOutputSockets()[0];
        gompeiJpegFile.assertSameImage(outputSocket.getValue().get());
    }

    @Test
    public void testConstructedWithIndex() {
        OutputSocket<Mat> outputSocket = sourceWithIndexSet.getOutputSockets()[0];
        gompeiJpegFile.assertSameImage(outputSocket.getValue().get());
    }

    @Test
    public void testLoadFromProperties() throws Exception {
        final Properties properties = sourceWithIndexSet.getProperties();
        final MultiImageFileSource newSource = new MultiImageFileSource();
        newSource.createFromProperties(new EventBus(), properties);
        OutputSocket<Mat> outputSocket = newSource.getOutputSockets()[0];
        gompeiJpegFile.assertSameImage(outputSocket.getValue().get());
    }
}