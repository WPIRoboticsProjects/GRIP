package edu.wpi.grip.core.operations.network.ros;


import com.google.common.testing.AbstractPackageSanityTests;

public class ROSPackageSanityTest extends AbstractPackageSanityTests {
  public ROSPackageSanityTest() {
    super();
    ignoreClasses(c -> c.equals(ROSLoader.class) || c.equals(MockROSManager.class));
    setDefault(JavaToMessageConverter.class, JavaToMessageConverter.BLOBS);
  }
}
