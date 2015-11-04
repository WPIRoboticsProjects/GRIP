package edu.wpi.grip.core.events;


public interface AddedEvent extends UndoableEvent {
    RemovedEvent createUndoEvent();
}
