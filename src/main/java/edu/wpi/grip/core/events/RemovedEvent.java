package edu.wpi.grip.core.events;

public interface RemovedEvent extends UndoableEvent {
    AddedEvent createUndoEvent();
}
