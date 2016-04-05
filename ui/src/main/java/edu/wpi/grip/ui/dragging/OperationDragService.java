package edu.wpi.grip.ui.dragging;


import com.google.inject.Singleton;
import edu.wpi.grip.core.Operation;
import edu.wpi.grip.core.OperationMetaData;

/**
 * Service for dragging an {@link Operation} from the {@link edu.wpi.grip.ui.pipeline.PipelineController}
 * to the {@link edu.wpi.grip.ui.pipeline.PipelineController}.
 */
@Singleton
public class OperationDragService extends DragService<OperationMetaData> {

    public OperationDragService() {
        super("operation");
    }
}
