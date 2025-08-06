package lev.learn.sandbox.harbor.connector.service

import lev.learn.sandbox.harbor.connector.connector.HarborConnector
import lev.learn.sandbox.harbor.connector.model.DockerRequest
import lev.learn.sandbox.harbor.connector.model.DockerRequestHeader
import lev.learn.sandbox.harbor.connector.model.DockerResponse
import lev.learn.sandbox.harbor.connector.model.DockerResponseChunked

import org.slf4j.LoggerFactory

class DockerService {
    private val connector = HarborConnector()
    private val logger = LoggerFactory.getLogger("DockerService")

    fun forwardHead(req: DockerRequest.Head): DockerResponse {
        logger.info("Service: forwardHead $req")
        return connector.requestHead(req)
    }

    fun forwardManifest(req: DockerRequest.Manifest): DockerResponse {
        logger.info("Service: forwardManifest $req")
        return connector.requestManifest(req)
    }

    suspend fun downloadBlob(request: DockerRequest.Blob): DockerResponse {
        // 0. Устанавливаем Range в запросе
        val firstRangeHeader = DockerRequestHeader("Range", "bytes=0-${CHUNK_SIZE - 1}")
        val rangedRequest = request.copy(
            headers = request.headers + firstRangeHeader
        )

        val firstResponse = connector.requestBlob(rangedRequest)

        val contentRange = firstResponse.contentRangeOrNull()
        val statusCode = firstResponse.statusCode()

        // 1. Ошибка
        if (statusCode !in listOf(200, 206)) {
            return firstResponse // можно обернуть в ошибку/response
        }

        // 2. Нет Content-Range → отдаем как есть
        if (contentRange == null) {
            println("!! Нет Content-Range → отдаем как есть ${request.path}")
            return firstResponse
        }

        // 3. Если вся длина уже получена → отдаем как есть
        val (start, end, total) = contentRange
        println("!! $start, $end, $total for ${request.path}")

        if (end + 1 >= total) {
            println("!! Полностью получен, отдаем как есть ${request.path}")
            return firstResponse
        }

        // 4. Иначе — нужно дозагружать чанками
        firstResponse.discard() // не читаем его, освобождаем

        val ranges: List<String> = generateRanges(0, total) // end + 1
        return DockerResponseChunked(ranges, request, connector)
    }

    fun generateRanges(start: Long, total: Long): List<String> {
        val ranges = mutableListOf<String>()
        var current = start
        while (current < total) {
            val end = (current + CHUNK_SIZE - 1).coerceAtMost(total - 1)
            ranges += "bytes=$current-$end"
            current = end + 1
        }
        return ranges
    }

    companion object {
        const val CHUNK_SIZE = 100L * 1024 * 1024 // 100mb
    }
}