package edu.wpi.grip.ui.codegeneration;

import com.google.common.base.CaseFormat;

import edu.wpi.grip.ui.codegeneration.data.TStep;


public class PythonTMethods extends TemplateMethods  {
  public PythonTMethods(){
    super();
  }

  @Override
  public String name(String name) {
	  return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_UNDERSCORE, name.replaceAll("\\s", ""));
  }

  @Override
  public String callOp(TStep step) {
    return null;
  }
}
