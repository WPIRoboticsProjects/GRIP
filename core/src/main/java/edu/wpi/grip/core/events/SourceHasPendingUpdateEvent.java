package edu.wpi.grip.core.events;


import edu.wpi.grip.core.Source;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An event that occurs when a source has an update pending for the {@link edu.wpi.grip.core.PipelineRunner} to handle.
 * This event should be triggered when a Source a value that is ready to be moved into
 * one or more of its sockets.
 * This event is not emitted to indicate that a socket has been changed, it just alerts the
 * {@link edu.wpi.grip.core.PipelineRunner} so that it can move the new value into the socket.
 * The pipeline will respond by running {@link Source#updateOutputSockets()} in the pipeline thread.
 */
public final class SourceHasPendingUpdateEvent implements RunPipelineEvent {
    private final Source source;

    public SourceHasPendingUpdateEvent(Source source) {
        this.source = checkNotNull(source, "The source can not be null");
    }

    public Source getSource() {
        return source;
    }

}
