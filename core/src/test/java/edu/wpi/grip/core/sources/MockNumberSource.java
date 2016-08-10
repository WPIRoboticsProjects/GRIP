package edu.wpi.grip.core.sources;

import edu.wpi.grip.core.Source;
import edu.wpi.grip.core.sockets.OutputSocket;
import edu.wpi.grip.core.sockets.SocketHint;
import edu.wpi.grip.core.sockets.SocketHints;
import edu.wpi.grip.core.util.ExceptionWitness.Factory;

import com.google.common.collect.ImmutableList;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class MockNumberSource extends Source {
  
  private static int numberOf = 0;
  private final int id;
  private final OutputSocket<Number> outputSocket;
  private final SocketHint<Number> outputSocketHint = 
      SocketHints.Outputs.createNumberSocketHint("Num", Math.PI);

  @SuppressFBWarnings(value = "ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD",
      justification = "Do not need to synchronize inside of a constructor")
  public MockNumberSource(Factory exceptionWitnessFactory, double value, OutputSocket.Factory osf) {
    super(exceptionWitnessFactory);
    id = numberOf++;
    outputSocket = osf.create(outputSocketHint);
    outputSocket.setValue(new Double(value));
  }

  @Override
  public String getName() {
    return "NumberSource" + id;
  }

  @Override
  protected List<OutputSocket> createOutputSockets() {
    return ImmutableList.of(
      outputSocket
    );
  }

  @Override
  protected boolean updateOutputSockets() {
    return false;
  }

  @Override
  public Properties getProperties() {
    final Properties properties = new Properties();
    properties.setProperty("number", createOutputSockets().get(0)
        .getValue().orElseGet(() -> Math.PI).toString());
    return properties;
  }

  @Override
  public void initialize() throws IOException {
    //Initialize doesn't need to do anything for a number source.
  }

}
