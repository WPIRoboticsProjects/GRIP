package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.opencv_core.FileStorage;

public enum FileStorageModeEnum {

    /** value, open the file for reading */
    READ(FileStorage.READ), /** value, open the file for writing */
    WRITE(FileStorage.WRITE), /** value, open the file for appending */
    APPEND(FileStorage.APPEND), /** flag, read data from source or write data to the internal buffer (which is
 *  returned by FileStorage::release) */
    MEMORY(FileStorage.MEMORY), /** mask for format flags */
    FORMAT_MASK(FileStorage.FORMAT_MASK), /** flag, auto format */
    FORMAT_AUTO(FileStorage.FORMAT_AUTO), /** flag, XML format */
    FORMAT_XML(FileStorage.FORMAT_XML), /** flag, YAML format */
    FORMAT_YAML(FileStorage.FORMAT_YAML);

    public final int value;

    FileStorageModeEnum(int value) {
        this.value = value;
    }
}
