package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.ui.codegeneration.data.TInput;
import edu.wpi.grip.ui.codegeneration.data.TStep;

import com.google.common.base.CaseFormat;

public class PythonTMethods extends TemplateMethods {
  public PythonTMethods() {
    super();
  }

  @Override
  public String name(String name) {
    return "__".concat(pyName(name));
  }

  public String pyName(String name) {
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_UNDERSCORE, name.replaceAll("\\s", ""));
  }

  @Override
  public String getterName(String name) {
    return name(name);
  }

  @Override
  public String setterName(String name) {
    return name(name);
  }

  @Override
  public String callOp(TStep step) {
    StringBuilder method = new StringBuilder(20);
    method.append("self.").append(name(step.name())).append('(');
    for (TInput inp : step.getInputs()) {
      method.append("self.").append(name(inp.name())).append(", ");
    }
    if (step.name().equals("Threshold_Moving")) {
      method.append("self.__lastImage");
      method.append(step.num());
      method.append(", ");
    }
    method.delete(method.length() - 2, method.length());
    method.append(')');
    return method.toString();
  }
}
