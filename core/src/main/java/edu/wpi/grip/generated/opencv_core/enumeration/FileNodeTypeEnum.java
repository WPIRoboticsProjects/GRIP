package edu.wpi.grip.generated.opencv_core.enumeration;

import org.bytedeco.javacpp.opencv_core;

public enum FileNodeTypeEnum {

    /** empty node */
    NONE(opencv_core.FileNode.NONE), /** an integer */
    INT(opencv_core.FileNode.INT), /** floating-point number */
    REAL(opencv_core.FileNode.REAL), /** synonym or REAL */
    FLOAT(opencv_core.FileNode.FLOAT), /** text string in UTF-8 encoding */
    STR(opencv_core.FileNode.STR), /** synonym for STR */
    STRING(opencv_core.FileNode.STRING), /** integer of size size_t. Typically used for storing complex dynamic structures where some elements reference the others */
    REF(opencv_core.FileNode.REF), /** sequence */
    SEQ(opencv_core.FileNode.SEQ), /** mapping */
    MAP(opencv_core.FileNode.MAP), TYPE_MASK(opencv_core.FileNode.TYPE_MASK), /** compact representation of a sequence or mapping. Used only by YAML writer */
    FLOW(opencv_core.FileNode.FLOW), /** a registered object (e.g. a matrix) */
    USER(opencv_core.FileNode.USER), /** empty structure (sequence or mapping) */
    EMPTY(opencv_core.FileNode.EMPTY), /** the node has a name (i.e. it is element of a mapping) */
    NAMED(opencv_core.FileNode.NAMED);

    public final int value;

    FileNodeTypeEnum(int value) {
        this.value = value;
    }
}
