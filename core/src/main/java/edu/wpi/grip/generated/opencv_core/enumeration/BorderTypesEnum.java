package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.global.opencv_core;

public enum BorderTypesEnum {

    /** `iiiiii|abcdefgh|iiiiiii`  with some specified `i` */
    BORDER_CONSTANT(opencv_core.BORDER_CONSTANT), /** `aaaaaa|abcdefgh|hhhhhhh` */
    BORDER_REPLICATE(opencv_core.BORDER_REPLICATE), /** `fedcba|abcdefgh|hgfedcb` */
    BORDER_REFLECT(opencv_core.BORDER_REFLECT), /** `cdefgh|abcdefgh|abcdefg` */
    BORDER_WRAP(opencv_core.BORDER_WRAP), /** `gfedcb|abcdefgh|gfedcba` */
    BORDER_REFLECT_101(opencv_core.BORDER_REFLECT_101), /** `uvwxyz|absdefgh|ijklmno` */
    BORDER_TRANSPARENT(opencv_core.BORDER_TRANSPARENT), /** same as BORDER_REFLECT_101 */
    BORDER_REFLECT101(opencv_core.BORDER_REFLECT101), /** same as BORDER_REFLECT_101 */
    BORDER_DEFAULT(opencv_core.BORDER_DEFAULT), /** do not look outside of ROI */
    BORDER_ISOLATED(opencv_core.BORDER_ISOLATED);

    public final int value;

    BorderTypesEnum(int value) {
        this.value = value;
    }
}
