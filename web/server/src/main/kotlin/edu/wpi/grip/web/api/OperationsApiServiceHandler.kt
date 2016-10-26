package edu.wpi.grip.web.api

import edu.wpi.grip.core.Palette
import edu.wpi.grip.web.swagger.api.OperationsApiService
import javax.inject.Inject
import javax.inject.Provider
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext
import edu.wpi.grip.web.swagger.model.OperationDescription as SwaggerOperationDescription


class OperationsApiServiceHandler @Inject constructor(val palette: Provider<Palette>) :
        OperationsApiService {

    override fun operationsGet(securityContext: SecurityContext): Response {
        val opList = palette
                .get()
                .operations
                .map { it.description.toSwagger() }
                .toList()
        return Response.ok(opList).build()
    }
}

