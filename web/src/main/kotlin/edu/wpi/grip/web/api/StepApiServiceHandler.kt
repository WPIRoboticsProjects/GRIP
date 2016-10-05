package edu.wpi.grip.web.api

import edu.wpi.grip.core.Palette
import edu.wpi.grip.core.Pipeline
import edu.wpi.grip.core.Step
import edu.wpi.grip.web.swagger.api.StepsApiService
import java.math.BigDecimal
import javax.inject.Inject
import javax.inject.Provider
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext


class StepApiServiceHandler @Inject constructor(
        val pipeline: Provider<Pipeline>,
        val palette: Provider<Palette>,
        val stepFactory: Step.Factory): StepsApiService {


    override fun stepsPut(operationName: String?, securityContext: SecurityContext?): Response {
        val operation = palette.get().getOperationByName(operationName)
        if (operation.isPresent) {
            val op = operation.get()
            val step = stepFactory.create(op);
            pipeline.get().addStep(step)

            return Response
                    .ok(step.toSwagger())
                    .build()
        } else {
            return Response
                    .status(Response.Status.BAD_REQUEST)
                    .entity("No operation matching this name")
                    .build()
        }
    }


    override fun stepsGet(index: BigDecimal, securityContext: SecurityContext): Response {
        try {
            val step = pipeline.get().steps.get(index = index.intValueExact())
            return Response.ok(step.toSwagger()).build()
        } catch (e: IndexOutOfBoundsException) {
            return Response
                    .status(Response.Status.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .entity("Index out of bound " + e.message).build()
        }
    }

}

fun Step.toSwagger(): edu.wpi.grip.web.swagger.model.Step {
    val swagStep = edu.wpi.grip.web.swagger.model.Step()
    swagStep.name = this.operationDescription.name();
    return swagStep
}
