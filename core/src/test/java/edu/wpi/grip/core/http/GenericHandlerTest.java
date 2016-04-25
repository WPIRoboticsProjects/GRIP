package edu.wpi.grip.core.http;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GenericHandlerTest {

    private GenericHandler gh;

    @Before
    public void setUp() {

    }

    @Test(expected = NullPointerException.class)
    public void testNullContext() {
        gh = new MockGenericHandler(null);
    }

    @Test
    public void testNoClaim() {
        gh = new MockGenericHandler();
        gh = new MockGenericHandler("testNoClaim");
        gh = new MockGenericHandler("testNoClaim", false);
        // No assert or fail -- an exception would be thrown if something went wrong
    }

    @Test(expected = IllegalArgumentException.class)
    public void testClaim() {
        String context = "testClaim";
        gh = new MockGenericHandler(context, true);
        assertTrue("Context should have been claimed", GenericHandler.isClaimed(context));
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

    private static class MockGenericHandler extends GenericHandler {

        MockGenericHandler() {
            super();
        }

        MockGenericHandler(String context) {
            super(context);
        }

        MockGenericHandler(String context, boolean doClaim) {
            super(context, doClaim);
        }

        @Override
        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        }
    }

}
