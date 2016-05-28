package edu.wpi.grip.core.sockets;

import com.google.common.testing.AbstractPackageSanityTests;

public class SocketsSanityTest extends AbstractPackageSanityTests {
    enum TestEnum {A, B, C}

    public SocketsSanityTest() {
        super();
        setDefault(Enum.class, TestEnum.A);
        ignoreClasses(c -> c.getName().contains("Mock"));
    }
}
