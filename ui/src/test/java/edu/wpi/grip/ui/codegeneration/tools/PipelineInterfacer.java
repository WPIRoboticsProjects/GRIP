package edu.wpi.grip.ui.codegeneration.tools;

import java.io.File;

public interface PipelineInterfacer {

  void setMatSource(int num, File img);

  void setNumSource(int num, Number val);
  
  void process();

  Object getOutput(int num, GenType type);

  void setSwitch(int num, boolean value);

  void setValve(int num, boolean value);

}
