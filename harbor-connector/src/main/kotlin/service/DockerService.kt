package lev.learn.sandbox.harbor.connector.service

import lev.learn.sandbox.harbor.connector.connector.HarborConnector
import lev.learn.sandbox.harbor.connector.model.ChunkedDockerResponse
import lev.learn.sandbox.harbor.connector.model.DockerRequest
import lev.learn.sandbox.harbor.connector.model.DockerResponse
import lev.learn.sandbox.harbor.connector.model.DockerResponse.Companion.contentLengthOrThrow
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

    fun forwardBlob(req: DockerRequest.Blob): DockerResponse {
        val chunkSize = 1 * 1024 * 1024L
        val maxBlobSize = 2 * 1024 * 1024L

        logger.info("Service: forwardBlob $req")

        // 1. HEAD-запрос, чтобы узнать размер blob
        val headResponse = connector.requestHead(DockerRequest.Head(req.path, req.headers))
        val contentLength = headResponse.contentLengthOrThrow()

        logger.info("Blob size : $contentLength bytes for ${req.path}")

        if (contentLength < maxBlobSize) {
            return connector.requestBlob(req)
        }

        // 2. Делим на чанки
        val ranges: List<String> = (0 until contentLength step chunkSize).map { start ->
            val end = minOf(start + chunkSize - 1, contentLength - 1)
            "bytes=$start-$end"
        }

        ranges.forEach {
            println("!!! $it => ${req.path}")
        }

        // 3. Возвращаем специальный DockerResponse, который будет объединять чанки потоком
        return ChunkedDockerResponse(
            ranges = ranges,
            baseRequest = req,
            connector = connector
        )
    }
}