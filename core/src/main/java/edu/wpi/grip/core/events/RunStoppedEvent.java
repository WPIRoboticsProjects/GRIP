package edu.wpi.grip.core.events;

/**
 * An event fired when the pipeline stops running. This is guaranteed to follow a corresponding
 * {@link RunStartedEvent}.
 *
 * <p>This is different from {@link RenderEvent} in that it will <i>always</i> be fired when the
 * pipeline runs.
 */
public class RunStoppedEvent {
}
