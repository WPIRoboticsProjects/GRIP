package edu.wpi.grip.core;

import edu.wpi.grip.core.cuda.AccelerationMode;
import edu.wpi.grip.core.cuda.CudaAccelerationMode;
import edu.wpi.grip.core.cuda.CudaDetector;
import edu.wpi.grip.core.cuda.CudaVerifier;
import edu.wpi.grip.core.cuda.LoadingCudaDetector;
import edu.wpi.grip.core.cuda.NullAccelerationMode;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.name.Names;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GripCudaModule extends AbstractModule {

  private static final Logger logger = Logger.getLogger(GripCudaModule.class.getName());
  private static final String CUDA_ENABLED_KEY = "edu.wpi.grip.cuda.enabled";

  @Override
  protected void configure() {
    bind(CudaDetector.class).to(LoadingCudaDetector.class);

    Properties cudaProperties = getCudaProperties();
    bind(Properties.class)
        .annotatedWith(Names.named("cudaProperties"))
        .toInstance(cudaProperties);

    if (Boolean.valueOf(cudaProperties.getProperty(CUDA_ENABLED_KEY, "false"))) {
      bind(AccelerationMode.class).to(CudaAccelerationMode.class);
    } else {
      bind(AccelerationMode.class).to(NullAccelerationMode.class);
    }

    bind(CudaVerifier.class).in(Scopes.SINGLETON);
  }

  private Properties getCudaProperties() {
    try (InputStream resourceAsStream = getClass().getResourceAsStream("CUDA.properties")) {
      Properties cudaProps = new Properties();
      cudaProps.load(resourceAsStream);
      return cudaProps;
    } catch (IOException e) {
      logger.log(Level.WARNING, "Could not read CUDA properties", e);
      return new Properties();
    }
  }

}
