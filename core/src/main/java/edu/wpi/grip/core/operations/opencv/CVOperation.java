package edu.wpi.grip.core.operations.opencv;

import edu.wpi.grip.core.Operation;

import java.io.InputStream;
import java.util.Optional;

public interface CVOperation extends Operation {

    @Override
    default Optional<InputStream> getIcon(){
        return Optional.of(
                getClass().getResourceAsStream("/edu/wpi/grip/ui/icons/opencv.png")
        );
    }
}
