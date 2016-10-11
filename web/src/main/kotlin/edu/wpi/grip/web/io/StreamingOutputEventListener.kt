package edu.wpi.grip.web.io

import com.google.common.eventbus.EventBus
import com.google.common.eventbus.Subscribe
import com.google.inject.servlet.SessionScoped
import edu.wpi.grip.core.events.SocketChangedEvent
import edu.wpi.grip.core.sockets.OutputSocket
import edu.wpi.grip.web.session.SessionDestroyedEvent
import edu.wpi.grip.web.session.SessionEventBus
import edu.wpi.grip.web.session.SessionEventRegistered
import org.bytedeco.javacpp.BytePointer
import org.bytedeco.javacpp.opencv_core.Mat
import org.bytedeco.javacpp.opencv_imgcodecs
import java.io.IOException
import java.io.OutputStream
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.concurrent.ThreadSafe
import javax.inject.Inject
import javax.inject.Provider
import javax.servlet.http.HttpSession
import javax.ws.rs.core.StreamingOutput


/**
 *
 */
@ThreadSafe
@SessionEventRegistered
open class StreamingOutputEventListener
private constructor(
        private val outputStreamHandler: (OutputStream, ByteArray) -> Unit,
        private val socket: OutputSocket<Mat>,
        private val httpSession: HttpSession) : StreamingOutput {
    private val sessionDestroyed = AtomicBoolean(false)
    private val imagePointer = BytePointer()
    private val lock = Object()

    @SessionScoped
    class Factory @Inject constructor(
            private val sessioneventBus: SessionEventBus,
            private val eventBus: EventBus,
            private val httpSessionProvider: Provider<HttpSession>) {

        fun create(outputStreamHandler: (OutputStream, ByteArray) -> Unit,
                   socket: OutputSocket<Mat>): StreamingOutputEventListener {
            val outputListener = StreamingOutputEventListener(
                    outputStreamHandler,
                    socket,
                    httpSessionProvider.get())
            eventBus.register(outputListener)
            sessioneventBus.register(outputListener)
            return outputListener
        }
    }

    override fun write(output: OutputStream) {
        try {
            do {
                val buffer = synchronized(lock) {
                    lock.wait()
                    if (sessionDestroyed.get()) return // Break out of the write

                    val byteBuffer = ByteArray(imagePointer.limit())
                    imagePointer.get(byteBuffer) // Load the image pointer into the buffer
                    byteBuffer
                }
                try {
                    // Pass the image pointer to be
                    outputStreamHandler.invoke(output, buffer)
                } catch (e: IOException) {
                    return
                }
            } while (!Thread.currentThread().isInterrupted && !sessionDestroyed.get())
        } finally {
            try {
                output.close()
            } catch (e: IOException) {
                // Ignore
            }
        }
    }

    @Subscribe
    fun onSocketChanged(changeEvent: SocketChangedEvent) {
        if (changeEvent.isRegarding(socket)) {
            socket.value.ifPresent {
                synchronized(lock) {
                    opencv_imgcodecs.imencode(".jpeg", socket.value.get(), imagePointer)
                    lock.notifyAll()
                }
            }
        }
    }

    @Subscribe
    fun onSessionDestroyed(sessionEvent: SessionDestroyedEvent) {
        if (sessionEvent.session == httpSession) {
            synchronized(lock) {
                sessionDestroyed.set(true)
                lock.notifyAll()
            }
        }
    }
}
