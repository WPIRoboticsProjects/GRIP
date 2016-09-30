package edu.wpi.grip.ui.preview;

import edu.wpi.grip.core.operations.network.MockGripNetworkModule;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.ui.GripUiModule;
import edu.wpi.grip.ui.util.MockGripPlatform;
import edu.wpi.grip.util.GripCoreTestModule;

import com.google.common.eventbus.EventBus;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

import org.bytedeco.javacpp.opencv_core.Point;
import org.bytedeco.javacpp.opencv_core.Size;
import org.junit.After;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import org.testfx.util.WaitForAsyncUtils;

import javafx.scene.Scene;
import javafx.stage.Stage;

import static org.testfx.api.FxAssert.verifyThat;

@RunWith(Enclosed.class)
public class PointSizeSocketPreviewViewTest {

  public static class PointSocketPreviewViewTest extends ApplicationTest {
    private static final String identifier = "testPoint";
    private static final int value1 = 1;
    private static final int value2 = 2;
    private GripCoreTestModule testModule;

    @Override
    public void start(Stage stage) {
      testModule = new GripCoreTestModule();
      testModule.setUp();
      final Injector injector = Guice.createInjector(Modules.override(testModule)
          .with(new GripUiModule(), new MockGripNetworkModule()));
      final OutputSocket<Point> pointOutputSocket =
          injector.getInstance(OutputSocket.Factory.class)
              .create(new SocketHint.Builder<>(Point.class)
                  .identifier(identifier)
                  .initialValueSupplier(() -> new Point(value1, value2))
                  .build());
      final PointSizeSocketPreviewView<Point> point =
          new PointSizeSocketPreviewView<>(new MockGripPlatform(new EventBus()), pointOutputSocket);
      final Scene scene = new Scene(point);
      stage.setScene(scene);
      stage.show();
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void testForExpectedValues() {
      WaitForAsyncUtils.waitForFxEvents();
      verifyThat(identifier, NodeMatchers.isVisible());
      verifyThat(Integer.toString(value1), NodeMatchers.isVisible());
      verifyThat(Integer.toString(value2), NodeMatchers.isVisible());
    }


    @After
    public void tearDown() {
      testModule.tearDown();
    }
  }

  public static class SizeSocketPreviewViewTest extends ApplicationTest {
    private static final String identifier = "testSize";
    private static final int value1 = 1;
    private static final int value2 = 2;
    private GripCoreTestModule testModule;

    @Override
    public void start(Stage stage) {
      testModule = new GripCoreTestModule();
      testModule.setUp();
      final Injector injector = Guice.createInjector(Modules.override(testModule)
          .with(new GripUiModule(), new MockGripNetworkModule()));
      final OutputSocket<Size> sizeOutputSocket =
          injector.getInstance(OutputSocket.Factory.class)
              .create(new SocketHint.Builder<>(Size.class)
                  .identifier(identifier)
                  .initialValueSupplier(() -> new Size(value1, value2))
                  .build());

      final PointSizeSocketPreviewView<Size> size =
          new PointSizeSocketPreviewView<>(new MockGripPlatform(new EventBus()), sizeOutputSocket);
      final Scene scene = new Scene(size);
      stage.setScene(scene);
      stage.show();
    }

    @Test
    @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
    public void testForExpectedValues() {
      WaitForAsyncUtils.waitForFxEvents();
      verifyThat(identifier, NodeMatchers.isVisible());
      verifyThat(Integer.toString(value1), NodeMatchers.isVisible());
      verifyThat(Integer.toString(value2), NodeMatchers.isVisible());
    }

    @After
    public void tearDown() {
      testModule.tearDown();
    }
  }


}
