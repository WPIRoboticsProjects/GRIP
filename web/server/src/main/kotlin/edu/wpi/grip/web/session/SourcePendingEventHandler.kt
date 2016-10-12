package edu.wpi.grip.web.session

import com.google.common.eventbus.Subscribe
import edu.wpi.grip.core.events.SourceHasPendingUpdateEvent
import javax.inject.Singleton

@Singleton
class SourcePendingEventHandler {

    @Subscribe
    fun onSourceHasPendingUpdateEvent(event: SourceHasPendingUpdateEvent) {
        event.source.updateOutputSocketsPublic()
    }
}
