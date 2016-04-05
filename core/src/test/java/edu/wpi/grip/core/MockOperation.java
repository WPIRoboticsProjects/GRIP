package edu.wpi.grip.core;


import com.google.common.collect.ImmutableList;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;

import java.util.List;

public class MockOperation implements Operation {
    public static final OperationDescription DESCRIPTION
            = OperationDescription.builder()
            .name("Mock Operation")
            .summary("A mock operation summary")
            .build();

    @Override
    public List<InputSocket> getInputSockets() {
        return ImmutableList.of();
    }

    @Override
    public List<OutputSocket> getOutputSockets() {
        return ImmutableList.of();
    }

    @Override
    public void perform() {

    }
}
