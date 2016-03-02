package edu.wpi.grip.core.util;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.security.Permission;

import static com.google.common.truth.Truth.assertThat;

public class SafeShutdownTest {
    public static final String SHUTDOWN_EXCEPTION_MESSAGE = "Tried to shut down VM";
    private static SecurityManager oldManager;

    protected static void setUpSecurityManager() {
        oldManager = System.getSecurityManager();
        System.setSecurityManager(new SecurityManager() {
            public void checkPermission( Permission permission ) {
                if( permission.getName().contains("exitVM") ) {
                    throw new IllegalStateException(SHUTDOWN_EXCEPTION_MESSAGE) ;
                }
            }
        });
    }

    protected static void tearDownSecurityManager() {
        System.setSecurityManager(oldManager);
    }


    @Before
    public void setUp(){
        setUpSecurityManager();
    }

    @After
    public void tearDown() {
        tearDownSecurityManager();
    }

    @Test
    public void testSafeShutdownShutsDownIfHandlerThrowsError() throws Exception {
        try {
            SafeShutdown.exit(0, () -> {
                throw new AssertionError("This should not be the exception that appears");
            });
        } catch (IllegalStateException e) {
            assertThat(e).hasMessage(SHUTDOWN_EXCEPTION_MESSAGE);
        }

    }



}
