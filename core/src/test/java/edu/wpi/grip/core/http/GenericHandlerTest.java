package edu.wpi.grip.core.http;

import org.eclipse.jetty.server.Request;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GenericHandlerTest {

  private GenericHandler gh;
  private final ContextStore store = new ContextStore();

  @Test(expected = NullPointerException.class)
  public void testNullContext() {
    gh = new MockGenericHandler(null);
  }

  @Test
  @SuppressWarnings("PMD.JUnitTestsShouldIncludeAssert")
  public void testNoClaim() {
    gh = new MockGenericHandler();
    gh = new MockGenericHandler("testNoClaim");
    gh = new MockGenericHandler("testNoClaim", false);
    // An exception will be thrown if something went wrong
  }

  @Test(expected = IllegalArgumentException.class)
  public void testClaim() {
    String context = "testClaim";
    gh = new MockGenericHandler(context, true);
    assertTrue("Context should have been claimed", store.contains(context));
    gh = new MockGenericHandler(context, true); // Should throw IllegalArgumentException
  }

  @Test
  public void testGetContext() {
    String context = "testGetContext";
    gh = new MockGenericHandler(context);
    assertEquals("Context was wrong", context, gh.getContext());
  }

  @After
  public void tearDown() {
    if (gh != null) {
      gh.releaseContext();
    }
  }

  private class MockGenericHandler extends GenericHandler {

    MockGenericHandler() {
      super();
    }

    MockGenericHandler(String context) {
      super(store, context);
    }

    MockGenericHandler(String context, boolean doClaim) {
      super(store, context, doClaim);
    }

    @Override
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException, ServletException {
      // Do nothing, this class is only used for testing automatic claims
    }
  }

}
