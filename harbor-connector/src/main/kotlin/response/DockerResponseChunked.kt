package lev.learn.sandbox.harbor.connector.response

import io.ktor.utils.io.*
import io.ktor.http.*
import io.ktor.http.content.OutgoingContent
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import lev.learn.sandbox.harbor.connector.connector.HarborConnector
import lev.learn.sandbox.harbor.connector.model.DockerRequest
import lev.learn.sandbox.harbor.connector.model.DockerRequestHeader
import org.slf4j.LoggerFactory
import kotlin.collections.plus

class DockerResponseChunked(
    private val firstResponse: DockerResponse,
    private val ranges: List<String>,
    private val baseRequest: DockerRequest.Blob,
    private val connector: HarborConnector
) : DockerResponse() {
    private val logger = LoggerFactory.getLogger("DockerResponseChunked")

    override fun statusCode(): Int = 206 // Partial Content

    override fun contentRangeOrNull(): Triple<Long, Long, Long>? = null

    override suspend fun body(): ByteReadChannel {
        error("Unsupported")
    }

    override suspend fun respondTo(call: ApplicationCall) {
        call.respond(object : OutgoingContent.WriteChannelContent() {
            override val contentType = ContentType.Application.OctetStream
            override val status = HttpStatusCode.PartialContent

            override suspend fun writeTo(channel: ByteWriteChannel) {
                // 1. Отправляем первый чанк
                val firstBody = firstResponse.body()
                try {
                    firstBody.copyTo(channel)
                } finally {
                    firstBody.cancel() // освобождаем ресурсы
                }

                // 2. Дозагружаем остальные чанки
                for (range in ranges) {
                    logger.debug("Получение порции данных $range для ${baseRequest.path}")

                    val reqWithRange = baseRequest.copy(
                        headers = baseRequest.headers + DockerRequestHeader(HttpHeaders.Range, range)
                    )

                    val response = connector.requestBlob(reqWithRange) as? DockerResponseBase
                        ?: error("Expected DockerResponseBase")

                    if (response.statusCode() != HttpStatusCode.PartialContent.value) {
                        error("Expected 206 Partial Content, got ${response.statusCode()}")
                    }

                    val chunkBody = response.body()
                    try {
                        chunkBody.copyTo(channel)
                    } finally {
                        chunkBody.cancel()
                        response.discard()
                    }
                }
            }
        })
    }
}
