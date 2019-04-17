package edu.wpi.grip.core.sources;

import edu.wpi.grip.core.MatWrapper;
import edu.wpi.grip.core.sockets.MockOutputSocketFactory;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.util.Files;
import edu.wpi.grip.util.ImageWithData;

import com.google.common.eventbus.EventBus;

import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 *
 */
public class ImageFileSourceTest {
  private final ImageWithData imageFile = Files.imageFile;
  private final File textFile = Files.textFile;
  private OutputSocket.Factory osf;

  @Before
  public void setUp() throws URISyntaxException {
    osf = new MockOutputSocketFactory(new EventBus());
  }

  @Test
  public void testLoadImageToMat() throws IOException {
    // Given above setup
    // When
    final ImageFileSource fileSource = new ImageFileSource(osf, origin -> null, this
        .imageFile.file);
    fileSource.initialize();
    OutputSocket<MatWrapper> outputSocket = fileSource.getOutputSockets().get(0);

    // Then
    assertTrue("The output socket's value was empty.", outputSocket.getValue().isPresent());

    imageFile.assertSameImage(outputSocket.getValue().get());
  }

  @Test(expected = IOException.class)
  public void testReadInTextFile() throws IOException {
    final ImageFileSource fileSource = new ImageFileSource(osf, origin -> null, this
        .textFile);
    fileSource.initialize();
    OutputSocket<MatWrapper> outputSocket = fileSource.getOutputSockets().get(0);
    assertTrue("No matrix should have been returned.", outputSocket.getValue().get().empty());
  }

  @Test(expected = IOException.class)
  public void testReadInFileWithoutExtension() throws MalformedURLException, IOException {
    final File testFile = new File("temp" + File.separator + "fdkajdl3eaf");

    final ImageFileSource fileSource = new ImageFileSource(osf, origin -> null, testFile);
    fileSource.initialize();
    fail("initialize() should have thrown an IOException");
  }

  @Test
  public void testCallingInitializeAfterGetOutputSocketUpdatesOutputSocket() throws IOException {
    final ImageFileSource source = new ImageFileSource(osf, origin -> null, this
        .imageFile.file);
    // Calling this before loading the image should throw an exception
    final OutputSocket<MatWrapper> imageSource = source.getOutputSockets().get(0);
    assertTrue("The value should not be present if the source hasn't been initialized",
        imageSource.getValue().get().empty());
    source.initialize();
    assertFalse("The value should now be present since the source has been initialized",
        imageSource.getValue().get().empty());
  }
}
