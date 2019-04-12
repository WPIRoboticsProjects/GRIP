package edu.wpi.grip.core;

import edu.wpi.grip.core.cuda.AccelerationMode;
import edu.wpi.grip.core.cuda.CudaAccelerationMode;
import edu.wpi.grip.core.cuda.CudaDetector;
import edu.wpi.grip.core.cuda.CudaVerifier;
import edu.wpi.grip.core.cuda.LoadingCudaDetector;
import edu.wpi.grip.core.cuda.NullAccelerationMode;
import edu.wpi.grip.core.util.MetaInfReader;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GripCudaModule extends AbstractModule {

  private static final Logger logger = Logger.getLogger(GripCudaModule.class.getName());

  @Override
  protected void configure() {
    bind(CudaDetector.class).to(LoadingCudaDetector.class);

    boolean usingCuda = false;
    try {
      usingCuda = MetaInfReader.readLines("HardwareAcceleration.mf")
          .anyMatch("Hardware-Acceleration: CUDA"::equals);
    } catch (IOException e) {
      logger.log(Level.WARNING, "Could not read manifest to determine CUDA acceleration", e);
    }
    if (usingCuda) {
      bind(AccelerationMode.class).to(CudaAccelerationMode.class);
    } else {
      bind(AccelerationMode.class).to(NullAccelerationMode.class);
    }

    bind(CudaVerifier.class).in(Scopes.SINGLETON);
  }

}
