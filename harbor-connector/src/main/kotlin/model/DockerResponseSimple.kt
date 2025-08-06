package lev.learn.sandbox.harbor.connector.model

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.readBytes
import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondOutputStream
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.cancel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DockerResponseSimple(
    private val response: HttpResponse,
    private val stream: Boolean = false
) : DockerResponse() {

    override fun statusCode(): Int = response.status.value

    override fun contentRangeOrNull(): Triple<Long, Long, Long>? {
        val header = response.headers[HttpHeaders.ContentRange] ?: return null

        val match = Regex("""bytes (\d+)-(\d+)/(\d+)""").matchEntire(header)
            ?: error("Invalid Content-Range format: $header")

        val (start, end, total) = match.destructured
        return Triple(start.toLong(), end.toLong(), total.toLong())
    }

    override suspend fun body(): ByteReadChannel = response.bodyAsChannel()

    override suspend fun bodyAsChannel(): ByteReadChannel {
        return if (stream) response.bodyAsChannel() else response.bodyAsChannel().also { discard() }
    }

    override suspend fun discard() {
        if (!stream) {
            response.bodyAsChannel().cancel()
        }
    }

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
                withContext(Dispatchers.IO) {
                    channel.copyTo(this@respondOutputStream)
                }
            }
        } else {
            val bytes = response.readBytes()
            call.respondBytes(bytes, status = response.status)
        }
    }
}
