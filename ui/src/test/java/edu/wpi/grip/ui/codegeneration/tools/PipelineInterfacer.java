package edu.wpi.grip.ui.codegeneration.tools;

import java.io.File;

public interface PipelineInterfacer {

  void setMatSource(int num, File img);

  void setNumSource(int num, Number val);
  
  void process();

  Object getOutput(String name, GenType type);

  void setSwitch(String name, boolean value);

  void setValve(String name, boolean value);

}
