package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum FileStorageEnum {

    UNDEFINED(opencv_core.FileStorage.UNDEFINED), VALUE_EXPECTED(opencv_core.FileStorage.VALUE_EXPECTED), NAME_EXPECTED(opencv_core.FileStorage.NAME_EXPECTED), INSIDE_MAP(opencv_core.FileStorage.INSIDE_MAP);

    public final int value;

    FileStorageEnum(int value) {
        this.value = value;
    }
}
