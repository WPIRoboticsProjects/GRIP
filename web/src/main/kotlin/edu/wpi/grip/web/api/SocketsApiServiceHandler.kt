package edu.wpi.grip.web.api

import edu.wpi.grip.core.Pipeline
import edu.wpi.grip.core.sockets.OutputSocket
import edu.wpi.grip.web.io.StreamingOutputEventListener
import edu.wpi.grip.web.swagger.api.SocketsApiService
import org.bytedeco.javacpp.opencv_core
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.inject.Inject
import javax.inject.Provider
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext
import javax.ws.rs.core.StreamingOutput


class SocketsApiServiceHandler @Inject constructor(
        val pipeline: Provider<Pipeline>,
        val streamingOutputEventListenerFactory: StreamingOutputEventListener.Factory) :
        SocketsApiService {
    val names = listOf("summer", "fall", "winter", "spring")
    val imageByteList: List<ByteArray>

    init {
        imageByteList = names.map {
            val image = File(javaClass.getResource(it + ".jpg").toURI())
            val originalImage = ImageIO.read(image)
            val baos = ByteArrayOutputStream()
            ImageIO.write(originalImage, "jpg", baos)
            baos.flush()
            val bytes = baos.toByteArray()
            baos.close()
            bytes // Returned
        }.toList()
    }

    override fun socketsImagesGet(securityContext: SecurityContext): Response {
        var currentIndex = 0
        val stream = StreamingOutput() {
            while (true) {
                if (currentIndex > 2) {
                    currentIndex = 0
                } else {
                    currentIndex++
                }
                try {
                    it.write(("--BoundaryString\r\n"
                            + "Content-type: image/jpeg\r\n"
                            + "Content-Length: "
                            + imageByteList[currentIndex].size
                            + "\r\n\r\n").toByteArray())
                    it.write(imageByteList[currentIndex])
                    it.write("\r\n\r\n".toByteArray())
                    it.flush()
                    TimeUnit.MILLISECONDS.sleep(50)
                } catch (e: IOException) {
                    break
                }
            }
        }

        val mediaType = MediaType("multipart", "x-mixed-replace", mapOf("boundary".to("BoundaryString")))
        return Response.ok(stream, mediaType).build()
    }

    override fun socketsUuidImagesGet(uuid: String, securityContext: SecurityContext): Response {
        val realUUID = UUID.fromString(uuid)
        val socket = pipeline.get().sources
                .flatMap { it.outputSockets }
                .filter { it.socketHint.type != opencv_core.Mat::class }
                .single { it.uuid == realUUID }
        val stream = streamingOutputEventListenerFactory.create({
            os, byte ->
            run {
                os.write(("--BoundaryString\r\n"
                        + "Content-type: image/jpeg\r\n"
                        + "Content-Length: "
                        + byte.size
                        + "\r\n\r\n").toByteArray())
                os.write(byte)
                os.write("\r\n\r\n".toByteArray())
                os.flush()
            }
        }, socket = socket as OutputSocket<opencv_core.Mat>)
        val mediaType = MediaType("multipart", "x-mixed-replace", mapOf("boundary".to("BoundaryString")))
        return Response.ok(stream, mediaType).build()
    }
}
