package edu.wpi.grip.web.api


import edu.wpi.grip.core.OperationDescription
import edu.wpi.grip.core.Step
import edu.wpi.grip.core.sockets.Socket
import edu.wpi.grip.core.sockets.SocketHint
import edu.wpi.grip.web.swagger.model.OperationDescription as SwaggerOperationDescription
import edu.wpi.grip.web.swagger.model.SocketHint as SwaggerSocketHint
import edu.wpi.grip.web.swagger.model.Step as SwaggerStep

/**
 * A set of converters for converting from the GRIP model to Swagger.
 */

/**
 * Converts between [OperationDescription] to the swagger version.
 */
fun OperationDescription.toSwagger(): SwaggerOperationDescription {
    val opDesc = SwaggerOperationDescription()
    opDesc.name = this.name()
    opDesc.summary = this.summary()
    opDesc.aliases = this.aliases().asList()
    opDesc.category =
            SwaggerOperationDescription.CategoryEnum
                    .valueOf(this.category().toString())
    return opDesc
}

/**
 * Converts between [Step] to the swagger version.
 */
fun Step.toSwagger(): SwaggerStep {
    val swagStep = SwaggerStep()
    swagStep.name = this.operationDescription.name()
    swagStep.uuid = this.uuid.toString()
    swagStep.operation = this.operationDescription.toSwagger()
    swagStep.inputSocket = this.inputSockets.map {it.toSwagger()}
    swagStep.outputSocket = this.outputSockets.map {it.toSwagger()}
    return swagStep
}

/**
 * Converts between [Socket] to the swagger version.
 */
fun <T> Socket<T>.toSwagger(): edu.wpi.grip.web.swagger.model.Socket {
    val socket = edu.wpi.grip.web.swagger.model.Socket()
    socket.uuid = this.uuid.toString()
    socket.socketHint = this.socketHint.toSwagger()
    return socket
}

/**
 * Converts between [SocketHint] to the swagger version.
 */
fun <T> SocketHint<T>.toSwagger() : SwaggerSocketHint {
    val socketHint = SwaggerSocketHint()
    socketHint.identifier = this.identifier
    socketHint.type = this.typeLabel
    return socketHint
}
