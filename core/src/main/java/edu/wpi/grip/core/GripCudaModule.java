package edu.wpi.grip.core;

import edu.wpi.grip.core.cuda.AccelerationMode;
import edu.wpi.grip.core.cuda.CudaAccelerationMode;
import edu.wpi.grip.core.cuda.CudaDetector;
import edu.wpi.grip.core.cuda.CudaVerifier;
import edu.wpi.grip.core.cuda.LinuxCudaDetector;
import edu.wpi.grip.core.cuda.MacCudaDetector;
import edu.wpi.grip.core.cuda.NullAccelerationMode;
import edu.wpi.grip.core.cuda.NullCudaDetector;
import edu.wpi.grip.core.cuda.WindowsCudaDetector;
import edu.wpi.grip.core.util.MetaInfReader;
import edu.wpi.grip.core.util.OperatingSystem;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GripCudaModule extends AbstractModule {

  private static final Logger logger = Logger.getLogger(GripCudaModule.class.getName());

  @Override
  protected void configure() {
    OperatingSystem os = OperatingSystem.forOsName(System.getProperty("os.name"));
    bind(OperatingSystem.class).toInstance(os);
    switch (os) {
      case WINDOWS:
        bind(CudaDetector.class).to(WindowsCudaDetector.class);
        break;
      case MAC:
        bind(CudaDetector.class).to(MacCudaDetector.class);
        break;
      case LINUX:
        bind(CudaDetector.class).to(LinuxCudaDetector.class);
        break;
      default:
        bind(CudaDetector.class).to(NullCudaDetector.class);
        break;
    }

    boolean usingCuda = false;
    try {
      usingCuda = MetaInfReader.readLines("MANIFEST.MF")
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
