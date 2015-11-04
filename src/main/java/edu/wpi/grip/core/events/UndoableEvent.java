package edu.wpi.grip.core.events;


public interface UndoableEvent {
    UndoableEvent createUndoEvent();
}
