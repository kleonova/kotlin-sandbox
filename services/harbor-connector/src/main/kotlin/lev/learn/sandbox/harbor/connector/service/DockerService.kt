package lev.learn.sandbox.harbor.connector.service

import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.availableForRead
import io.ktor.utils.io.copyTo
import io.ktor.utils.io.writer
import kotlinx.coroutines.coroutineScope
import lev.learn.sandbox.harbor.connector.config.ConfigLoader
import lev.learn.sandbox.harbor.connector.connector.HarborConnector
import lev.learn.sandbox.harbor.connector.model.DockerRequest
import lev.learn.sandbox.harbor.connector.model.DockerRequestHeader
import lev.learn.sandbox.harbor.connector.response.ClientGetResponse
import lev.learn.sandbox.harbor.connector.response.DockerResponse

import org.slf4j.LoggerFactory

class DockerService {
    private val connector = HarborConnector()
    private val logger = LoggerFactory.getLogger("DockerService")

    private companion object {
        private val config by lazy { ConfigLoader.loadChunkConfig() }
        val CHUNK_SIZE = config.sizeBytes
    }

    fun forwardHead(req: DockerRequest.Head): DockerResponse {
        logger.info("Service: forwardHead $req")
        return connector.requestHead(req)
    }

    fun forwardManifest(req: DockerRequest.Manifest): DockerResponse {
        logger.info("Service: forwardManifest $req")
        return connector.requestManifest(req)
    }

    suspend fun downloadBlob(
        request: DockerRequest.Blob,
        action: suspend (ClientGetResponse) -> Unit
    ) {
        coroutineScope {
            val channel: ByteReadChannel = writer {
                var totalSize: Long? = null
                var start = 0L

                while (true) {
                    val end = start + CHUNK_SIZE - 1
                    val range = "bytes=$start-$end"

                    val requestWithRange = request.addHeader(DockerRequestHeader(HttpHeaders.Range, range))

                    val connectorResponse = connector.getRange(requestWithRange)

                    // из первого запроса получаем totalSize из Content-Range
                    if (totalSize == null) {
                        totalSize = parseTotalSize(connectorResponse.headers)
                        logger.info("Service → определили общий размер: $totalSize байт")
                    }

                    val lengthResponse = connectorResponse.headers[HttpHeaders.ContentLength]?.toLong()
                        ?: error("Нет Content-Length в ответе")
                    logger.info("Service → стримим $range, ожидаем $lengthResponse байт")

                    val copied = connectorResponse.channel.copyTo(channel, limit = lengthResponse)
                    logger.info("✓ скопировано $copied байт")

                    // пределяем следующую порцию
                    start += lengthResponse
                    if (totalSize == null) break
                    if (start >= totalSize) break
                }
            }.channel

            val totalRealSize = channel.availableForRead.toString()
            logger.info("Service → определили общий размер CHANNEL: $totalRealSize байт")

            action(
                ClientGetResponse(
                    channel,
                    headersOf(HttpHeaders.ContentLength, totalRealSize)
                )
            )
        }
    }

    /**
     * Разбирает строку Content-Range и возвращает total
     * Пример: "bytes 27262976-28230269/28230270" → 28230270
     */
    private fun parseTotalSize(headers: Headers): Long? {
        val header = headers[HttpHeaders.ContentRange] ?: return null

        val match = Regex("""bytes (\d+)-(\d+)/(\d+)""").matchEntire(header)
            ?: error("Неверный формат Content-Range: $header")

        val (_, _, total) = match.destructured

        return total.toLong()
    }
}
