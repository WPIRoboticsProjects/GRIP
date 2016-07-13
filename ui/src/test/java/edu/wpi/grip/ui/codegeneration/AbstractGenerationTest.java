package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.core.Pipeline;
import edu.wpi.grip.core.events.SourceAddedEvent;
import edu.wpi.grip.core.operations.OperationsUtil;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sources.ImageFileSource;
import edu.wpi.grip.ui.codegeneration.tools.CppPipelineInterfacer;
import edu.wpi.grip.ui.codegeneration.tools.HelperTools;
import edu.wpi.grip.ui.codegeneration.tools.JavaPipelineInterfacer;
import edu.wpi.grip.ui.codegeneration.tools.PipelineCreator;
import edu.wpi.grip.ui.codegeneration.tools.PipelineGenerator;
import edu.wpi.grip.ui.codegeneration.tools.PipelineInterfacer;
import edu.wpi.grip.ui.codegeneration.tools.PythonPipelineInterfacer;
import edu.wpi.grip.util.GripCoreTestModule;
import edu.wpi.grip.util.ImageWithData;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;

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

import javax.inject.Inject;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Category(GenerationTest.class)
public abstract class AbstractGenerationTest {
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
  protected PipelineGenerator gen;

  static {
    System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
  }

  @Before
  public void setUp() {
    testModule = new GripCoreTestModule();
    testModule.setUp();
    final Injector injector = Guice.createInjector(testModule);
    injector.injectMembers(this);
    gen = new PipelineGenerator();
    injector.injectMembers(gen);
  }

  protected final void test(BooleanSupplier setup, Consumer<PipelineInterfacer> test, String
      testName) {
    assertTrue("Setup for " + testName + " reported an issue.", setup.getAsBoolean());
    String fileName = testName;
    gen.export(fileName);

    JavaPipelineInterfacer jpip = new JavaPipelineInterfacer(fileName + ".java");
    PythonPipelineInterfacer ppip = new PythonPipelineInterfacer(fileName);
    CppPipelineInterfacer cpip = new CppPipelineInterfacer(fileName);
    Language current = Language.JAVA;
    try {
      test.accept(jpip);
      current = Language.PYTHON;
      test.accept(ppip);
      current = Language.CPP;
      test.accept(cpip);
    } catch (AssertionError e) {
      //Makes it easier to tell which language is throwing an error.
      StringBuilder msg = new StringBuilder();
      msg.append("In ").append(current).append(": ");
      if (e.getMessage() != null) {
        msg.append(e.getMessage());
      }
      AssertionError error = new AssertionError(msg.toString(), e.getCause());
      error.setStackTrace(e.getStackTrace());
      throw error;
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
    File dir = PipelineGenerator.codeDir.getAbsoluteFile();
    try {
      File[] toDelete = dir.listFiles((File file) -> {
        String name = file.getName();
        return name.contains(".cpp") || name.contains(".py") || name.contains(".h")
            || name.contains(".dylib") || name.contains(".so") || name.contains(".dll");
      });
      for (File file : toDelete) {
        Files.deleteIfExists(file.toPath());
      }
      Files.deleteIfExists(img.toPath());
      Files.deleteIfExists(testing.toPath());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  protected ImageFileSource loadImage(ImageWithData img) {
    ImageFileSource out = imgfac.create(img.file);
    try {
      out.initialize();
    } catch (IOException e) {
      e.printStackTrace();
      fail("IO Exception occurred while loading Image " + img.file.getName());
    }
    eventBus.post(new SourceAddedEvent(out));
    return out;
  }

  protected void assertMatWithin(Mat gen, Mat grip, double tolerance) {
    double diff = Math.abs(HelperTools.matAvgDiff(gen, grip));
    assertTrue("Difference between two Mats was: " + diff
            + ", which is greater than tolerance of: " + tolerance,
        diff <= tolerance);
  }
}
