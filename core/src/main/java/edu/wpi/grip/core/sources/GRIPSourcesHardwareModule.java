package edu.wpi.grip.core.sources;

import com.google.inject.AbstractModule;

/**
 * Adds bindings for hardware that is required by {@link edu.wpi.grip.core.Source Sources}
 */
public class GRIPSourcesHardwareModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(CameraSource.FrameGrabberFactory.class).to(CameraSource.FrameGrabberFactoryImpl.class);
    }
}
