package lev.learn.sandbox.harbor.connector.model

import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondOutputStream
import io.ktor.utils.io.jvm.javaio.copyTo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import lev.learn.sandbox.harbor.connector.connector.HarborConnector
import org.slf4j.LoggerFactory

class ChunkedDockerResponse(
    private val ranges: List<String>,
    private val baseRequest: DockerRequest.Blob,
    private val connector: HarborConnector
) : DockerResponse(
    response = dummyResponse(), // нужен, чтобы базовый класс не упал
    stream = true
) {
    private val logger = LoggerFactory.getLogger("ChunkedDockerResponse")

    override suspend fun respondTo(call: ApplicationCall) {
        call.response.status(HttpStatusCode.OK)
        call.response.headers.append(HttpHeaders.AcceptRanges, "bytes")

        call.respondOutputStream {
            withContext(Dispatchers.IO) {
                for (range in ranges) {
                    val partReq = baseRequest.copy(
                        headers = baseRequest.headers + DockerRequestHeader(HttpHeaders.Range, range)
                    )

                    val chunkResponse = connector.requestBlob(partReq)
                    val channel = chunkResponse.rawBodyChannel()

                    logger.info("Streaming range: $range")
                    channel.copyTo(this@respondOutputStream)
                }
            }
        }
    }

    companion object {
        private fun dummyResponse(): HttpResponse {
            throw IllegalStateException("This response is not used directly.")
        }
    }
}