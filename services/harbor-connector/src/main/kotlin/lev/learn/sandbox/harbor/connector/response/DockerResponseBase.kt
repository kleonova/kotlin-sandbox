package lev.learn.sandbox.harbor.connector.response

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.client.statement.readRawBytes
import io.ktor.http.Headers
import io.ktor.http.HeadersBuilder
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondBytes
import io.ktor.server.response.respondOutputStream
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.cancel
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory

class DockerResponseBase(
    private val response: HttpResponse,
    private val stream: Boolean = false
) : DockerResponse() {
    private val logger = LoggerFactory.getLogger("DockerResponseBase")
    private val dispatcher = Dispatchers.IO

    private companion object {
        val PROHIBITED_HEADERS = setOf(
            HttpHeaders.TransferEncoding,
            HttpHeaders.ContentLength,
            HttpHeaders.Connection,
        )
    }

    override fun statusCode(): Int = response.status.value

    override fun contentRangeOrNull(): Triple<Long, Long, Long>? {
        val header = response.headers[HttpHeaders.ContentRange] ?: return null

        val match = Regex("""bytes (\d+)-(\d+)/(\d+)""").matchEntire(header)
            ?: error("Invalid Content-Range format: $header")

        val (start, end, total) = match.destructured
        return Triple(start.toLong(), end.toLong(), total.toLong())
    }

    override suspend fun body(): ByteReadChannel = response.bodyAsChannel()

    override suspend fun discard() {
        if (!stream) {
            response.bodyAsChannel().cancel()
        }
    }

    override suspend fun respondTo(call: ApplicationCall) {
        val statusCode = if (response.status == HttpStatusCode.PartialContent) {
            HttpStatusCode.OK
        } else {
            response.status
        }

        // Собираем заголовки, исключая Content-Range и Content-Length
        val headersBuilder = HeadersBuilder()

        response.headers.forEach { key, values ->
            val lowerKey = key.lowercase()
            if (lowerKey != HttpHeaders.ContentRange.lowercase() &&
                lowerKey != HttpHeaders.ContentLength.lowercase()
            ) {
                values.forEach { value ->
                    headersBuilder.append(key, value)
                }
            }
        }

        if (!stream) {
            val bytes = response.readRawBytes()
            headersBuilder[HttpHeaders.ContentLength] = bytes.size.toString()
            call.setHeaders(headersBuilder.build())
            call.respondBytes(bytes, status = statusCode)
        } else {
            val channel = response.bodyAsChannel()
            response.headers[HttpHeaders.ContentLength]?.toLongOrNull()?.let {
                headersBuilder[HttpHeaders.ContentLength] = it.toString()
            }
            call.setHeaders(headersBuilder.build())
            call.respondOutputStream(status = statusCode) {
                withContext(dispatcher) {
                    channel.copyTo(this@respondOutputStream)
                }
            }
        }
    }

    private fun ApplicationCall.setHeaders(headers: Headers) {
        headers.forEach { key, values ->
            if (key !in PROHIBITED_HEADERS) {
                values.forEach { value ->
                    this.response.headers.append(key, value)
                }
            } else {
                logger.trace("Пропускаем управляемый заголовок: $key")
            }
        }
    }
}
