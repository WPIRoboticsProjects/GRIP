package edu.wpi.grip.core.sources;


import com.google.inject.Guice;
import com.google.inject.Injector;
import edu.wpi.grip.core.GRIPCoreModule;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.fail;

public class CameraSourceTest {
    private Injector injector;
    private CameraSource.Factory cameraSourceFactory;

    @Before
    public void setUp() throws Exception {
        this.injector = Guice.createInjector(new GRIPCoreModule());
        this.cameraSourceFactory = injector.getInstance(CameraSource.Factory.class);
    }

    @Test(expected = IOException.class)
    public void testInvalidURLThrowsIOException() throws Exception {
        cameraSourceFactory.create("Not a URL at all!");
        fail("This test should have thrown an exception.");
    }

}