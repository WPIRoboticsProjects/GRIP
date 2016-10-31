package edu.wpi.grip.core.sockets;

import com.google.common.testing.AbstractPackageSanityTests;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class SocketsSanityTest extends AbstractPackageSanityTests {
  public SocketsSanityTest() {
    super();
    setDefault(Enum.class, TestEnum.A);
    ignoreClasses(c -> c.getName().contains("Mock"));
  }

  enum TestEnum {
    A, B, C
  }
}
