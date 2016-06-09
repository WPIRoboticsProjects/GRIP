package edu.wpi.grip.core.operations;

import com.google.common.collect.ImmutableList;
import com.google.common.eventbus.EventBus;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.Step;
import edu.wpi.grip.core.util.MockExceptionWitness;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@RunWith(Parameterized.class)
public class OperationsTest {

    @Parameter
    public OperationMetaData operationMetaData;

    @Parameters(name = "{index}: Operation({0})")
    public static Collection<Object[]> data() {
        EventBus eventBus = new EventBus();
        List<OperationMetaData> operationMetaDatas =
                ImmutableList.<OperationMetaData>builder()
                        .addAll(
                                OperationsFactory
                                        .create(eventBus)
                                        .operations())
                        .addAll(
                                OperationsFactory
                                        .createCV(eventBus)
                                        .operations())
                        .build();

        Object[][] params = new Object[operationMetaDatas.size()][1];
        final int[] index = {0};
        operationMetaDatas.forEach(operationMeta -> {
            params[index[0]][0] = operationMeta;
            index[0]++;
        });
        return Arrays.asList(params);
    }


    @Test
    public void testCreateAllSteps() {
        final Step step =
                new Step.Factory((origin) -> new MockExceptionWitness(new EventBus(), origin)).create(operationMetaData);
        step.setRemoved();
    }
}
