package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum SortFlagsEnum {

    /** each matrix row is sorted independently */
    SORT_EVERY_ROW(opencv_core.SORT_EVERY_ROW), /** each matrix column is sorted
 *  independently; this flag and the previous one are
 *  mutually exclusive. */
    SORT_EVERY_COLUMN(opencv_core.SORT_EVERY_COLUMN), /** each matrix row is sorted in the ascending
 *  order. */
    SORT_ASCENDING(opencv_core.SORT_ASCENDING), /** each matrix row is sorted in the
 *  descending order; this flag and the previous one are also
 *  mutually exclusive. */
    SORT_DESCENDING(opencv_core.SORT_DESCENDING);

    public final int value;

    SortFlagsEnum(int value) {
        this.value = value;
    }
}
