package edu.wpi.grip.core;


import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import edu.wpi.grip.core.events.RedoPublishedEvent;
import edu.wpi.grip.core.events.UndoPublishedEvent;
import edu.wpi.grip.core.events.UndoableEvent;

import java.util.Optional;
import java.util.Stack;

public class EventHistory {
    private final Stack<UndoableEvent> undoableEvents = new Stack<>();
    private final Stack<UndoableEvent> redoableEvents = new Stack();
    private final EventBus eventBus;
    private Optional<UndoableEvent> lastEventPosted = Optional.empty();

    public EventHistory(EventBus eventBus){
        this.eventBus = eventBus;
        eventBus.register(this);
    }

    @Subscribe
    public void onUndoPublishedEvent(UndoPublishedEvent event){
        publishedEventHandler(undoableEvents, redoableEvents);
    }

    @Subscribe
    public void onRedoPublishedEvent(RedoPublishedEvent event){
        publishedEventHandler(redoableEvents, undoableEvents);
    }

    private void publishedEventHandler(Stack<UndoableEvent> eventStack, Stack<UndoableEvent> otherStack){
        if (!eventStack.isEmpty()){
            final UndoableEvent undoableEvent = eventStack.pop().createUndoEvent();
            otherStack.push(undoableEvent);
            this.lastEventPosted = Optional.of(undoableEvent);
            this.eventBus.post(undoableEvent);
        }
    }

    @Subscribe
    public void onUndoableEvent(UndoableEvent event){
        // If we didn't just post this event ourselves
        if (!lastEventPosted.equals(Optional.of(event))) {
            undoableEvents.push(event);
        }
    }
}
