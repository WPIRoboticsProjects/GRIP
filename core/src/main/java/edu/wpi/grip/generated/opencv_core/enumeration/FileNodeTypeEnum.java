package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.opencv.opencv_core.FileNode;

public enum FileNodeTypeEnum {

    /** empty node */
    NONE(FileNode.NONE), /** an integer */
    INT(FileNode.INT), /** floating-point number */
    REAL(FileNode.REAL), /** synonym or REAL */
    FLOAT(FileNode.FLOAT), /** text string in UTF-8 encoding */
    STR(FileNode.STR), /** synonym for STR */
    STRING(FileNode.STRING), /** integer of size size_t. Typically used for storing complex dynamic structures where some elements reference the others */
    SEQ(FileNode.SEQ), /** mapping */
    MAP(FileNode.MAP),
    TYPE_MASK(FileNode.TYPE_MASK), /** compact representation of a sequence or mapping. Used only by YAML writer */
    FLOW(FileNode.FLOW), /** a registered object (e.g. a matrix) */
    USER(FileNode.UNIFORM), /** empty structure (sequence or mapping) */
    EMPTY(FileNode.EMPTY), /** the node has a name (i.e. it is element of a mapping) */
    NAMED(FileNode.NAMED);

    public final int value;

    FileNodeTypeEnum(int value) {
        this.value = value;
    }
}
