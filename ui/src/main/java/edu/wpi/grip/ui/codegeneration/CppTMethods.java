package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.ui.codegeneration.data.TInput;
import edu.wpi.grip.ui.codegeneration.data.TStep;

import com.google.common.base.CaseFormat;

public class CppTMethods extends TemplateMethods {
  public CppTMethods() {
    super();
  }

  @Override
  public String name(String name) {
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name.toUpperCase());
  }

  @Override
  public String callOp(TStep step) {
    String num = "S" + step.num();
    StringBuilder out = new StringBuilder();
    if (step.name().equals("Switch") || step.name().equals("Valve")) {
      out.append("pipeline");
    }
    out.append(name(step.name()));
    out.append("(");
    for (TInput input : step.getInputs()) {
      if (!input.type().equals("List")) {
        out.append("&");
      }
      out.append(input.name());
      out.append(num);
      out.append(", ");
    }
    if (step.name().equals("Threshold_Moving")) {
      out.append("this->lastImage");
      out.append(num);
      out.append(", ");
    }
    if (!step.getOutputs().isEmpty()) {
      for (int i = 0; i < step.getOutputs().size() - 1; i++) {
        out.append("this->");
        out.append(step.getOutputs().get(i).name());
        out.append(", ");
      }
      out.append("this->");
      out.append(step.getOutputs().get(step.getOutputs().size() - 1).name());
    }
    out.append(")");
    return out.toString();
  }


}
