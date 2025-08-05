package lev.learn.sandbox.harbor.connector.model

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.readBytes
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondOutputStream
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class DockerResponse (
    private val response: HttpResponse,
    private val stream: Boolean = false
) {
    private val dispatcher = Dispatchers.IO

    open suspend fun respondTo(call: ApplicationCall) {
        response.headers.forEach { key, values ->
            if (!key.equals(HttpHeaders.ContentLength, ignoreCase = true)) {
                values.forEach { value ->
                    call.response.headers.append(key, value, safeOnly = false)
                }
            }
        }

        if (stream) {
            val channel = response.bodyAsChannel()

            call.respondOutputStream(status = response.status) {
                withContext(dispatcher) {
                    channel.copyTo(this@respondOutputStream)
                }
            }
        } else {
            val bytes = response.readBytes()
            call.respondBytes(bytes, status = response.status)
        }
    }

    open suspend fun rawBodyChannel(): ByteReadChannel = response.bodyAsChannel()

    companion object {
        fun DockerResponse.contentLengthOrThrow(): Long {
            val value: String = response.headers[HttpHeaders.ContentLength]
                ?: error("Content-Length not found in HEAD response")


            val value2: String = response.headers["Content-Length"] ?: error("Content-Length not found in HEAD response")

            println("!! contentLength = $value VS $value2")

            return value.toLongOrNull()
                ?: error("Invalid Content-Length: $value")
        }
    }
}