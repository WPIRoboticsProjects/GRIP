package edu.wpi.grip.web.api

import edu.wpi.grip.core.Pipeline
import edu.wpi.grip.core.events.SourceAddedEvent
import edu.wpi.grip.core.sources.CameraSource
import edu.wpi.grip.web.swagger.api.SourcesApiService
import javax.inject.Inject
import javax.inject.Provider
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext


class SourcesApiServiceHandler @Inject constructor(
        val pipeline : Provider<Pipeline>,
        val sourceFactory : CameraSource.Factory) :
        SourcesApiService {
    override fun sourcesPut(securityContext: SecurityContext?): Response {
        val source = sourceFactory.create(0)
        pipeline.get().onSourceAdded(SourceAddedEvent(source))
        source.initializeSafely()
        return sourcesGet(securityContext)
    }

    override fun sourcesGet(securityContext: SecurityContext?): Response {
        return Response.ok(pipeline.get().sources.map { it.toSwagger() }.toList()).build()
    }
}
