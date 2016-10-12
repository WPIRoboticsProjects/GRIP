package edu.wpi.grip.web.api

import edu.wpi.grip.core.Pipeline
import edu.wpi.grip.web.swagger.api.SourcesApiService
import javax.inject.Inject
import javax.inject.Provider
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext


class SourcesApiServiceHandler @Inject constructor(val pipeline : Provider<Pipeline>) :
        SourcesApiService {
    override fun sourcesGet(securityContext: SecurityContext?): Response {
        return Response.ok(pipeline.get().sources.map { it.toSwagger() }.toList()).build()
    }
}
