package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum FileStorageModeEnum {

    /** value, open the file for reading */
    READ(opencv_core.FileStorage.READ), /** value, open the file for writing */
    WRITE(opencv_core.FileStorage.WRITE), /** value, open the file for appending */
    APPEND(opencv_core.FileStorage.APPEND), /** flag, read data from source or write data to the internal buffer (which is
 *  returned by FileStorage::release) */
    MEMORY(opencv_core.FileStorage.MEMORY), /** mask for format flags */
    FORMAT_MASK(opencv_core.FileStorage.FORMAT_MASK), /** flag, auto format */
    FORMAT_AUTO(opencv_core.FileStorage.FORMAT_AUTO), /** flag, XML format */
    FORMAT_XML(opencv_core.FileStorage.FORMAT_XML), /** flag, YAML format */
    FORMAT_YAML(opencv_core.FileStorage.FORMAT_YAML);

    public final int value;

    FileStorageModeEnum(int value) {
        this.value = value;
    }
}
