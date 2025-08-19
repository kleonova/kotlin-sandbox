package lev.learn.sandbox.harbor.connector.service

import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.cancel
import io.ktor.utils.io.close
import io.ktor.utils.io.copyTo
import io.ktor.utils.io.writer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import lev.learn.sandbox.harbor.connector.config.ConfigLoader
import lev.learn.sandbox.harbor.connector.connector.HarborConnector
import lev.learn.sandbox.harbor.connector.model.DockerRequest
import lev.learn.sandbox.harbor.connector.model.DockerRequestHeader
import lev.learn.sandbox.harbor.connector.response.ClientGetResponse
import lev.learn.sandbox.harbor.connector.response.DockerResponse
import lev.learn.sandbox.harbor.connector.response.DockerResponseChunked

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

    fun downloadBlob(request: DockerRequest.Blob): DockerResponse {
        // Проверяем, есть ли уже Range в запросе
        val existingRange = request.headers
            .find { it.key.equals(HttpHeaders.Range, ignoreCase = true) }
            ?.value

        val firstRangeValue: String = when {
            existingRange == null -> {
                // Нет Range — начинаем с 0
                "bytes=0-${CHUNK_SIZE - 1}"
            }
            existingRange.contains("bytes=([^-,]+)-$".toRegex()) -> {
                // Формат: bytes=12345-  → добавляем конец
                val startStr = existingRange.substringAfter("bytes=").substringBefore("-")
                val start = startStr.toLongOrNull()
                    ?: error("Invalid Range header: start is not a number: $existingRange")

                val end = start + CHUNK_SIZE - 1
                "bytes=$start-$end"
            }
            else -> {
                // Остальные форматы оставляем как есть:
                // - bytes=123-456
                // - bytes=-500
                existingRange
            }
        }

        val firstRangeHeader = DockerRequestHeader(HttpHeaders.Range, firstRangeValue)
        val rangedRequest = request.copy(
            headers = request.headers + firstRangeHeader
        )

        val firstResponse: DockerResponse = connector.requestBlob(rangedRequest)

        val contentRange = firstResponse.contentRangeOrNull()
        val statusCode = firstResponse.statusCode()

        // Ошибка
        if (statusCode !in listOf(200, 206)) {
            return firstResponse
        }

        // Нет Content-Range → отдаем как есть (например, 200 OK без пагинации)
        if (contentRange == null) {
            logger.debug("Отсутствует заголовок `Content-Range` в ответе `${request.path}`")
            return firstResponse
        }

        val (_, end, total) = contentRange

        // Если уже получили весь диапазон (или бэкенд вернул всё) — возвращаем как есть
        if (end + 1 >= total) {
            logger.debug("Размер слоя `$total` не превышает размер установленной порции: ($CHUNK_SIZE байт), ${request.path}")
            return firstResponse
        }

        // Нужно дозагружать оставшиеся порции
        val ranges = generateRanges(end + 1, total)
        logger.debug("Требуется загрузка порциями: ${ranges.size} по $CHUNK_SIZE байт, ${request.path}")
        return DockerResponseChunked(firstResponse, ranges, request, connector)
    }

    suspend fun getBlob(
        request: DockerRequest,
        action: suspend (ClientGetResponse) -> Unit
    ) {
        val chunkSize = 1 * 1024 * 1024
        val totalSize = 28230270 // знаем общий размер

        val ranges = (0 until totalSize step chunkSize).map { start ->
            val end = (start + chunkSize - 1).coerceAtMost(totalSize - 1)
            "bytes=$start-$end"
        }

        coroutineScope {
            val channel: ByteReadChannel = writer {
                for (range in ranges) {
                    val connectorResponse = connector.getRange(request.path, range)
                    val lengthResponse = connectorResponse.headers[HttpHeaders.ContentLength]?.toLong()
                        ?: error("Нет Content-Length в ответе")

                    logger.info("Service → стримим $range, ожидаем $lengthResponse байт")

                    val copied = connectorResponse.channel.copyTo(channel, limit = lengthResponse)
                    logger.info("✓ скопировано $copied байт")
                }
            }.channel

            action(
                ClientGetResponse(
                    channel,
                    headersOf(HttpHeaders.ContentLength, totalSize.toString())
                )
            )
        }
    }

    private fun generateRanges(start: Long, total: Long): List<String> {
        val ranges = mutableListOf<String>()
        var current = start
        while (current < total) {
            val end = (current + CHUNK_SIZE - 1).coerceAtMost(total - 1)
            ranges += "bytes=$current-$end"
            current = end + 1
        }
        return ranges
    }

    suspend fun copyExactLength(src: ByteReadChannel, dst: ByteWriteChannel, length: Long) {
        var remaining = length
        while (remaining > 0) {
            val packet = src.readRemaining(minOf(8192, remaining))
            val size = packet.remaining.toLong()
            if (size == 0L) break
            dst.writePacket(packet)
            remaining -= size
        }
        dst.flush()
    }
}
