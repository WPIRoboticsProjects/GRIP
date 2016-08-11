package edu.wpi.grip.core.util;


import com.google.common.testing.AbstractPackageSanityTests;

@SuppressWarnings("PMD.TestClassWithoutTestCases")
public class UtilitySanityTest extends AbstractPackageSanityTests {
  public UtilitySanityTest() {
    super();
    publicApiOnly();
    ignoreClasses(c -> c.getName().contains("Mock"));
  }

  @SuppressWarnings("PMD.JUnit4TestShouldUseBeforeAnnotation")
  @Override
  public void setUp() {
    SafeShutdownTest.setUpSecurityManager();
  }

  @SuppressWarnings("PMD.JUnit4TestShouldUseAfterAnnotation")
  @Override
  public void tearDown() {
    SafeShutdownTest.tearDownSecurityManager();
  }
}
