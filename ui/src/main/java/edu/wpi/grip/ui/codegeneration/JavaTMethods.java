package edu.wpi.grip.ui.codegeneration;

import edu.wpi.grip.ui.codegeneration.data.TInput;
import edu.wpi.grip.ui.codegeneration.data.TStep;

import com.google.common.base.CaseFormat;

public class JavaTMethods extends TemplateMethods {
  public JavaTMethods() {
    super();
  }

  @Override
  public String name(String name) {
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name.replaceAll("\\s", ""));
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
    StringBuilder out = new StringBuilder();
    out.append(name(step.name())).append('(');
    for (TInput input : step.getInputs()) {
      out.append(name(input.name()));
      out.append(", ");
    }
    if (step.name().equals("Threshold_Moving")) {
      out.append("this.lastImage");
      out.append(step.num());
      out.append(", ");
    }
    if (!step.getOutputs().isEmpty()) {
      for (int i = 0; i < step.getOutputs().size(); i++) {
        out.append(name(step.getOutputs().get(i).name()));
        if (step.getOutput(i).mutable()) {
          out.append("Ref");
        }
        out.append(", ");
      }
      //removes the unneeded ", "
      out.delete(out.length() - 2, out.length());
    }
    out.append(')');
    return out.toString();
  }

}
