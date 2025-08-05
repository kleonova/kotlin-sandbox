package lev.learn.sandbox.harbor.connector.controller

import io.ktor.server.application.ApplicationCall
import lev.learn.sandbox.harbor.connector.model.HarborConnectorRequest
import lev.learn.sandbox.harbor.connector.model.HarborConnectorResponse
import lev.learn.sandbox.harbor.connector.service.HarborConnectorService
import org.slf4j.LoggerFactory

class HarborConnectorController {
    private val service = HarborConnectorService()
    private val logger = LoggerFactory.getLogger("HarborConnectorController")

    fun buildRequest(call: ApplicationCall, type: String): HarborConnectorRequest {
        val path = call.parameters.getAll("path")?.joinToString("/") ?: ""
        logger.info("Controller: building request for type=$type, path=$path")

        return when (type) {
            "HEAD" -> HarborConnectorRequest.Head(path)
            "GET_MANIFEST" -> HarborConnectorRequest.Manifest(path)
            "GET_BLOB" -> HarborConnectorRequest.Blob(path)
            else -> error("Unknown request type: $type")
        }
    }

    fun handleHead(req: HarborConnectorRequest.Head): HarborConnectorResponse {
        logger.info("Controller: handleHead $req")
        return service.forwardHead(req)
    }

    fun handleManifest(req: HarborConnectorRequest.Manifest): HarborConnectorResponse {
        logger.info("Controller: handleManifest $req")
        return service.forwardManifest(req)
    }

    fun handleBlob(req: HarborConnectorRequest.Blob): HarborConnectorResponse {
        logger.info("Controller: handleBlob $req")
        return service.forwardBlob(req)
    }
}