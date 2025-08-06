package lev.learn.sandbox.harbor.connector.controller

import io.ktor.server.application.ApplicationCall
import lev.learn.sandbox.harbor.connector.model.DockerRequest
import lev.learn.sandbox.harbor.connector.model.DockerRequestHeader
import lev.learn.sandbox.harbor.connector.model.DockerResponse
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

    fun handleHead(req: DockerRequest.Head): DockerResponse {
        logger.info("Controller: handleHead $req")
        return service.forwardHead(req)
    }

    fun handleManifest(req: DockerRequest.Manifest): DockerResponse {
        logger.info("Controller: handleManifest $req")
        return service.forwardManifest(req)
    }

    suspend fun handleBlob(req: DockerRequest.Blob): DockerResponse {
        logger.info("Controller: handleBlob $req")
        return service.downloadBlob(req)
    }
}