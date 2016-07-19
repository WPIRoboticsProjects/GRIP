package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.ui.codegeneration.data.TInput;
import edu.wpi.grip.ui.codegeneration.data.TStep;

import com.google.common.base.CaseFormat;

import java.util.Locale;

public class CppTMethods extends TemplateMethods {
  public CppTMethods() {
    super();
  }

  @Override
  public String name(String name) {
    Locale locName = new Locale(name);
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, locName.toString().toUpperCase());
  }

  @Override
  public String callOp(TStep step) {
    StringBuilder out = new StringBuilder();
    if (step.name().equals("Switch") || step.name().equals("Valve")) {
      out.append("pipeline");
    }
    out.append(name(step.name())).append('(');
    for (TInput input : step.getInputs()) {
      out.append(input.name());
      out.append(", ");
    }
    if (step.name().equals("Threshold_Moving")) {
      out.append("this->lastImage");
      out.append(step.num());
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
    out.append(')');
    return out.toString();
  }


}
