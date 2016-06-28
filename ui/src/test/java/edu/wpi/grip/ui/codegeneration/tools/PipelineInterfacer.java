package edu.wpi.grip.ui.codegeneration.tools;

import java.io.File;


public interface PipelineInterfacer {

  void setSource(int num, Object value);

  void setSourceAsObject(int num, Object value);

  void setMatSource(int num, File img);

  void process();

  Object getOutput(int num, GenType type);

  void setSwitch(int num, boolean value);

  void setValve(int num, boolean value);

}
