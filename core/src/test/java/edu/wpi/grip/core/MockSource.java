package edu.wpi.grip.core;


import java.io.IOException;
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
    protected OutputSocket[] createOutputSockets() {
        return new OutputSocket[0];
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