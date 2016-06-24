package edu.wpi.grip.core.operations;
import javax.inject.Inject;

import com.google.common.collect.ImmutableList;
import static org.junit.Assert.fail;
import edu.wpi.grip.core.OperationMetaData;
public class OperationsUtil {
	@Inject
	private static CVOperations cvOperations;
	public static ImmutableList<OperationMetaData> operations(){
		return cvOperations.operations();
	}
	public static OperationMetaData getMetaData(String opName){
		for(OperationMetaData data : operations()){
			if(data.getDescription().name().equalsIgnoreCase(opName)){
				return data;
			}
		}
		fail("Given operation name " + opName + " does not match any CV operation");
		return null;//Never going to happen since line above throws error
	}
}
