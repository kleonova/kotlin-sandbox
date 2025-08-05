package lev.learn.sandbox.harbor.connector.model

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.readBytes
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondOutputStream
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HarborConnectorResponse (
    private val response: HttpResponse,
    private val stream: Boolean = false
) {
    private val dispatcher = Dispatchers.IO

    suspend fun respondTo(call: ApplicationCall) {
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
}