package edu.wpi.grip.core.cuda;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CudaVerifierTest {

  @Test
  public void testVerifyNonCudaRuntime() {
    CudaVerifier verifier = new CudaVerifier(new NullAccelerationMode(), new NullCudaDetector());
    assertTrue("Not using CUDA should always pass verification", verifier.verify());
  }

  @Test
  public void testUsingCudaWithNoAvailableRuntime() {
    CudaVerifier verifier = new CudaVerifier(() -> true, new NullCudaDetector());
    assertFalse("Using CUDA but no available runtime should fail verification", verifier.verify());
  }

  @Test
  public void testExitsWhenFailsVerification() {
    NonExitingVerifier verifier = new NonExitingVerifier(() -> true, new NullCudaDetector());

    verifier.verifyCuda();
    assertEquals(1, verifier.getExitCount());
  }

  private static final class NonExitingVerifier extends CudaVerifier {

    private int exitCount = 0;

    public NonExitingVerifier(AccelerationMode accelerationMode, CudaDetector cudaDetector) {
      super(accelerationMode, cudaDetector);
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
