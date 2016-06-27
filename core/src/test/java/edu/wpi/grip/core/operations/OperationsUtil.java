package edu.wpi.grip.core.operations;

import edu.wpi.grip.core.OperationMetaData;

import com.google.common.collect.ImmutableList;

import javax.inject.Inject;

import static org.junit.Assert.fail;

public class OperationsUtil {
  @Inject
  private CVOperations cvOperations;

  public ImmutableList<OperationMetaData> operations() {
    return cvOperations.operations();
  }

  public OperationMetaData getMetaData(String opName) {
    opName = opName.toLowerCase().replaceAll("[^a-zA-Z]", "");
    for (OperationMetaData data : operations()) {
      String dataName = data.getDescription().name().toLowerCase().replaceAll("[^a-zA-Z]", "");
      if(dataName.equals(opName)){
        return data;
      }
    }
    fail("Given operation name " + opName + " does not match any CV operation");
    return null; //Never going to happen since line above throws error
  }
}
