package edu.wpi.grip.core.operations;

import edu.wpi.grip.core.FileManager;
import edu.wpi.grip.core.OperationMetaData;
import edu.wpi.grip.core.operations.composite.BlurOperation;
import edu.wpi.grip.core.operations.composite.ConvexHullsOperation;
import edu.wpi.grip.core.operations.composite.DesaturateOperation;
import edu.wpi.grip.core.operations.composite.DistanceTransformOperation;
import edu.wpi.grip.core.operations.composite.FilterContoursOperation;
import edu.wpi.grip.core.operations.composite.FilterLinesOperation;
import edu.wpi.grip.core.operations.composite.FindBlobsOperation;
import edu.wpi.grip.core.operations.composite.FindContoursOperation;
import edu.wpi.grip.core.operations.composite.FindLinesOperation;
import edu.wpi.grip.core.operations.composite.HSLThresholdOperation;
import edu.wpi.grip.core.operations.composite.HSVThresholdOperation;
import edu.wpi.grip.core.operations.composite.MaskOperation;
import edu.wpi.grip.core.operations.composite.NormalizeOperation;
import edu.wpi.grip.core.operations.composite.PublishVideoOperation;
import edu.wpi.grip.core.operations.composite.RGBThresholdOperation;
import edu.wpi.grip.core.operations.composite.ResizeOperation;
import edu.wpi.grip.core.operations.composite.SaveImageOperation;
import edu.wpi.grip.core.operations.composite.SwitchOperation;
import edu.wpi.grip.core.operations.composite.ThresholdMoving;
import edu.wpi.grip.core.operations.composite.ValveOperation;
import edu.wpi.grip.core.operations.composite.WatershedOperation;
import edu.wpi.grip.core.operations.opencv.MatFieldAccessor;
import edu.wpi.grip.core.operations.opencv.MinMaxLoc;
import edu.wpi.grip.core.operations.opencv.NewPointOperation;
import edu.wpi.grip.core.operations.opencv.NewSizeOperation;
import edu.wpi.grip.core.sockets.InputSocket;
import edu.wpi.grip.core.sockets.OutputSocket;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import static com.google.common.base.Preconditions.checkNotNull;

@Singleton
public class BasicOperations implements Operations {

  private final ImmutableList<OperationMetaData> operations;

  @Inject
  BasicOperations(FileManager fileManager,
                  InputSocket.Factory isf,
                  OutputSocket.Factory osf) {
    checkNotNull(fileManager, "fileManager cannot be null");
    this.operations = ImmutableList.of(
        // Composite operations
        new OperationMetaData(BlurOperation.DESCRIPTION,
            () -> new BlurOperation(isf, osf)),
        new OperationMetaData(ConvexHullsOperation.DESCRIPTION,
            () -> new ConvexHullsOperation(isf, osf)),
        new OperationMetaData(DesaturateOperation.DESCRIPTION,
            () -> new DesaturateOperation(isf, osf)),
        new OperationMetaData(DistanceTransformOperation.DESCRIPTION,
            () -> new DistanceTransformOperation(isf, osf)),
        new OperationMetaData(FilterContoursOperation.DESCRIPTION,
            () -> new FilterContoursOperation(isf, osf)),
        new OperationMetaData(FilterLinesOperation.DESCRIPTION,
            () -> new FilterLinesOperation(isf, osf)),
        new OperationMetaData(FindBlobsOperation.DESCRIPTION,
            () -> new FindBlobsOperation(isf, osf)),
        new OperationMetaData(FindContoursOperation.DESCRIPTION,
            () -> new FindContoursOperation(isf, osf)),
        new OperationMetaData(FindLinesOperation.DESCRIPTION,
            () -> new FindLinesOperation(isf, osf)),
        new OperationMetaData(HSLThresholdOperation.DESCRIPTION,
            () -> new HSLThresholdOperation(isf, osf)),
        new OperationMetaData(HSVThresholdOperation.DESCRIPTION,
            () -> new HSVThresholdOperation(isf, osf)),
        new OperationMetaData(MaskOperation.DESCRIPTION,
            () -> new MaskOperation(isf, osf)),
        new OperationMetaData(NormalizeOperation.DESCRIPTION,
            () -> new NormalizeOperation(isf, osf)),
        new OperationMetaData(PublishVideoOperation.DESCRIPTION,
            () -> new PublishVideoOperation(isf)),
        new OperationMetaData(ResizeOperation.DESCRIPTION,
            () -> new ResizeOperation(isf, osf)),
        new OperationMetaData(RGBThresholdOperation.DESCRIPTION,
            () -> new RGBThresholdOperation(isf, osf)),
        new OperationMetaData(SaveImageOperation.DESCRIPTION,
            () -> new SaveImageOperation(isf, osf, fileManager)),
        new OperationMetaData(SwitchOperation.DESCRIPTION,
            () -> new SwitchOperation(isf, osf)),
        new OperationMetaData(ValveOperation.DESCRIPTION,
            () -> new ValveOperation(isf, osf)),
        new OperationMetaData(WatershedOperation.DESCRIPTION,
            () -> new WatershedOperation(isf, osf)),
        new OperationMetaData(ThresholdMoving.DESCRIPTION,
            () -> new ThresholdMoving(isf, osf)),

        // OpenCV operations
        new OperationMetaData(MatFieldAccessor.DESCRIPTION,
            () -> new MatFieldAccessor(isf, osf)),
        new OperationMetaData(MinMaxLoc.DESCRIPTION,
            () -> new MinMaxLoc(isf, osf)),
        new OperationMetaData(NewPointOperation.DESCRIPTION,
            () -> new NewPointOperation(isf, osf)),
        new OperationMetaData(NewSizeOperation.DESCRIPTION,
            () -> new NewSizeOperation(isf, osf))
    );
  }

  @VisibleForTesting
  public ImmutableList<OperationMetaData> operations() {
    return operations;
  }
}
