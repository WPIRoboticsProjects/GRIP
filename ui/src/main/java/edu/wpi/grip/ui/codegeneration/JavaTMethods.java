package edu.wpi.grip.ui.codegeneration;

import com.google.common.base.CaseFormat;

import edu.wpi.grip.ui.codegeneration.java.TInput;
import edu.wpi.grip.ui.codegeneration.java.TStep;

/**
 * Created by Toby on 6/13/16.
 */
public class JavaTMethods extends TemplateMethods  {
  public JavaTMethods(){
    super();
  }

  @Override
  public String name(String name) {
    return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name.replaceAll("\\s", ""));
  }

  @Override
  public String callOp(TStep step) {
    String num = "S" + step.num();
    StringBuilder out = new StringBuilder();
    out.append(name(step.name()));
    out.append("(");
    for (TInput input : step.getInputs()) {
      out.append(input.name());
      out.append(num);
      out.append(", ");
    }
    if (step.name().equals("Threshold_Moving")) {
      out.append("this.lastImage");
      out.append(num);
      out.append(", ");
    }
    if (!step.getOutputs().isEmpty()) {
      for (int i = 0; i < step.getOutputs().size() - 1; i++) {
        out.append(step.getOutputs().get(i).name());
        out.append(", ");
      }
      out.append(step.getOutputs().get(step.getOutputs().size() - 1).name());
    }
    out.append(")");
    return out.toString();
  }

}
