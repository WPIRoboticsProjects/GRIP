package edu.wpi.grip.core;


import com.google.common.collect.ImmutableList;
import edu.wpi.grip.core.sockets.OutputSocket;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class MockSource extends Source {

    protected MockSource() {
        super(origin -> null);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    protected List<OutputSocket> createOutputSockets() {
        return ImmutableList.of();
    }

    @Override
    protected boolean updateOutputSockets() {
        return false;
    }

    @Override
    public Properties getProperties() {
        return null;
    }

    @Override
    public void initialize() throws IOException {

    }
}