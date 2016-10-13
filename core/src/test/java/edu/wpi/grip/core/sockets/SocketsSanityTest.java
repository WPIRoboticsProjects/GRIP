package edu.wpi.grip.core.sockets;

import com.google.common.testing.AbstractPackageSanityTests;

import java.util.UUID;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class SocketsSanityTest extends AbstractPackageSanityTests {
  public SocketsSanityTest() {
    super();
    setDefault(Enum.class, TestEnum.A);
    ignoreClasses(c -> c.getName().contains("Mock"));
    setDefault(UUID.class, UUID.randomUUID());
  }

  enum TestEnum {
    A, B, C
  }
}
