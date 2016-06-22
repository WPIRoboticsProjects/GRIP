package edu.wpi.grip.ui.codegeneration;

import com.google.common.base.CaseFormat;

import edu.wpi.grip.ui.codegeneration.data.TInput;
import edu.wpi.grip.ui.codegeneration.data.TStep;


public class PythonTMethods extends TemplateMethods {
  public PythonTMethods() {
    super();
  }

  @Override
  public String name(String name) {
    return "__".concat(CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_UNDERSCORE, name.replaceAll("\\s", "")));
  }

  @Override
  public String callOp(TStep step) {
    StringBuilder method = new StringBuilder();
    method.append("self.").append(name(step.name())).append("(");
    for (TInput inp : step.getInputs()) {
      method.append("self.__").append(inp.name()).
          append("s").append(step.num()).append(", ");
    }
    if (step.name().equals("Threshold_Moving")) {
      method.append("self.__lastImage");
      method.append(step.num());
      method.append(", ");
    }
    method.delete(method.length() - 2, method.length());
    method.append(")");
    return method.toString();
  }
}
