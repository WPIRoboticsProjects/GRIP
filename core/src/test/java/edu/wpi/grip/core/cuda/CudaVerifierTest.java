package edu.wpi.grip.core.cuda;

import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CudaVerifierTest {

  @Test
  public void testVerifyNonCudaRuntime() {
    CudaVerifier verifier = new NonExitingVerifier(() -> false, () -> false);
    assertTrue("Not using CUDA should always pass verification", verifier.verify());
  }

  @Test
  public void testUsingCudaWithNoAvailableRuntime() {
    CudaVerifier verifier = new NonExitingVerifier(() -> true, () -> false);
    assertFalse("Using CUDA but no available runtime should fail verification", verifier.verify());
  }

  @Test
  public void testExitsWhenFailsVerification() {
    NonExitingVerifier verifier = new NonExitingVerifier(() -> true, () -> false);

    verifier.verifyCuda();
    assertEquals(1, verifier.getExitCount());
  }

  private static final class NonExitingVerifier extends CudaVerifier {

    private int exitCount = 0;

    public NonExitingVerifier(AccelerationMode accelerationMode, CudaDetector cudaDetector) {
      super(accelerationMode, cudaDetector, new Properties());
    }

    @Override
    void exit() {
      exitCount++;
    }

    public int getExitCount() {
      return exitCount;
    }
  }

}
