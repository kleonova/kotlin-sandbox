package lev.learn.sandbox.harbor.connector.controller

import io.ktor.http.ContentType
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respondBytesWriter
import io.ktor.utils.io.copyTo
import lev.learn.sandbox.harbor.connector.model.DockerRequest
import lev.learn.sandbox.harbor.connector.model.DockerRequestHeader
import lev.learn.sandbox.harbor.connector.response.ClientGetResponse
import lev.learn.sandbox.harbor.connector.response.DockerResponse
import lev.learn.sandbox.harbor.connector.service.DockerService
import org.slf4j.LoggerFactory

class DockerController {
    private val service = DockerService()
    private val logger = LoggerFactory.getLogger("HarborConnectorController")

    fun buildRequest(call: ApplicationCall, type: String): DockerRequest {
        val path = call.parameters.getAll("path")?.joinToString("/") ?: ""
        logger.info("Controller: building request for type=$type, path=$path")

        val headers: List<DockerRequestHeader> = call.request.headers.entries().flatMap { (key, values) ->
            values.map { value ->
                DockerRequestHeader(key, value)
            }
        }

        return when (type) {
            "HEAD" -> DockerRequest.Head(path, headers)
            "GET_MANIFEST" -> DockerRequest.Manifest(path, headers)
            "GET_BLOB" -> DockerRequest.Blob(path, headers)
            else -> error("Unknown request type: $type")
        }
    }

    fun handleHead(request: DockerRequest.Head): DockerResponse {
        logger.info("Controller: handleHead $request")
        return service.forwardHead(request)
    }

    fun handleManifest(request: DockerRequest.Manifest): DockerResponse {
        logger.info("Controller: handleManifest $request")
        return service.forwardManifest(request)
    }

    fun handleBlob(request: DockerRequest.Blob): DockerResponse {
        logger.info("Controller: handleBlob $request")
        return service.downloadBlob(request)
    }

    suspend fun handleStreamBlob(request: DockerRequest, action: suspend (ClientGetResponse) -> Unit) {
        logger.info("Controller: handleStreamBlob ${request.path}")

        service.getBlob(request) {
            println("Controller →")
            action(it)
        }
    }
}