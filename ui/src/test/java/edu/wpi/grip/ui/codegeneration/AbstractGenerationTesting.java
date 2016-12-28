package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.events.SourceAddedEvent;
import edu.wpi.grip.core.operations.OperationsUtil;
import edu.wpi.grip.core.operations.network.MockGripNetworkModule;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.JavaPipelineInterfacer;
import edu.wpi.grip.ui.codegeneration.tools.PipelineCreator;
import edu.wpi.grip.ui.codegeneration.tools.PipelineGenerator;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.util.GripCoreTestModule;
import edu.wpi.grip.util.ImageWithData;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.experimental.categories.Category;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category(GenerationTesting.class)
@SuppressWarnings("PMD.JUnit4TestShouldUseTestAnnotation")
public class AbstractGenerationTesting {
  private static final Logger logger = Logger.getLogger(AbstractGenerationTesting.class.getName());
  private GripCoreTestModule testModule;
  @Inject
  protected EventBus eventBus;
  @Inject
  protected Pipeline pipeline;
  @Inject
  protected InputSocket.Factory isf;
  @Inject
  protected OutputSocket.Factory osf;
  @Inject
  protected ImageFileSource.Factory imgfac;
  @Inject
  protected OperationsUtil opUtil;
  @Inject
  protected PipelineGenerator gen;

  static {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

  @Before
  public void setUp() {
    testModule = new GripCoreTestModule();
    testModule.setUp();
    final Injector injector = Guice.createInjector(Modules.override(testModule)
            .with(new MockGripNetworkModule()));
    injector.injectMembers(this);
    injector.injectMembers(gen);
  }

  protected final void test(BooleanSupplier setup, Consumer<PipelineInterfacer> test,
                            String testName) {
    assertTrue("Setup for " + testName + " reported an issue.", setup.getAsBoolean());
    String fileName = testName;
    gen.export(fileName);
    Language current = Language.JAVA;
    try {
      JavaPipelineInterfacer jpip = new JavaPipelineInterfacer(fileName + ".java");
      test.accept(jpip);

      // The opencv-python package is broken, so python tests won't work on Travis
      //current = Language.PYTHON;
      //PythonPipelineInterfacer ppip = new PythonPipelineInterfacer(fileName);
      //test.accept(ppip);

      // C++ is just plain broken
      //current = Language.CPP;
      //CppPipelineInterfacer cpip = new CppPipelineInterfacer(fileName);
      //test.accept(cpip);
    } catch (Throwable e) {
      StringBuilder msg = new StringBuilder(20);
      msg.append("In ").append(current).append(" there was a ").append(e.getClass().getName())
          .append('\n');
      if (e.getMessage() != null) {
        msg.append(e.getMessage());
      }
      AssertionError ae = new AssertionError(msg.toString(), e.getCause());
      ae.setStackTrace(e.getStackTrace());
      logger.log(Level.WARNING, e.getMessage(), e);
      throw ae; //NOPMD
    }
  }

  @After
  public void tearDown() {
    testModule.tearDown();
  }

  /**
   * Cleans up generated test java and class files.
   */
  @AfterClass
  public static void tearDownClass() {
    PipelineCreator.cleanClasses();
    File img = new File("img.png");
    File testing = new File("testing.py");
    File output = new File("output.txt");
    File dir = PipelineGenerator.getCodeDir().getAbsoluteFile();
    try {
      File[] toDelete = dir.listFiles((File file) -> {
        String name = file.getName();
        return name.contains(".cpp") || name.contains(".py") || name.contains(".h")
            || name.contains(".dylib") || name.contains(".so") || name.contains(".dll")
            || name.contains(".lib") || name.contains(".vcxproj") || name.contains(".exp");
      });
      if (toDelete != null) {
        for (File file : toDelete) {
          Files.deleteIfExists(file.toPath());
        }
      }
      Files.deleteIfExists(img.toPath());
      Files.deleteIfExists(testing.toPath());
      Files.deleteIfExists(output.toPath());
    } catch (IOException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
    }
  }

  protected ImageFileSource loadImage(ImageWithData img) {
    ImageFileSource out = imgfac.create(img.file);
    try {
      out.initialize();
    } catch (IOException e) {
      logger.log(Level.WARNING, e.getMessage(), e);
      fail("IO Exception occurred while loading Image " + img.file.getName());
    }
    eventBus.post(new SourceAddedEvent(out));
    return out;
  }

  protected void assertMatWithin(Mat gen, Mat grip, double tolerance) {
    double diff = Math.abs(HelperTools.matAvgDiff(gen, grip));
    assertTrue("Difference between two Mats was: " + diff + ", which is greater than tolerance of: "
        + tolerance, diff <= tolerance);
  }
}
