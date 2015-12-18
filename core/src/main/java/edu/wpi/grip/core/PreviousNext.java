package edu.wpi.grip.core;


/**
 * An Object that can switch its value.
 */
public interface PreviousNext {

    /**
     * Perform the next action on this object.
     */
    void next();

    /**
     * Perform the previous action on this object.
     */
    void previous();

}
