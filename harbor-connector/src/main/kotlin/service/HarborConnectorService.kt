package lev.learn.sandbox.harbor.connector.service

import lev.learn.sandbox.harbor.connector.connector.HarborConnector
import lev.learn.sandbox.harbor.connector.model.HarborConnectorRequest
import lev.learn.sandbox.harbor.connector.model.HarborConnectorResponse
import org.slf4j.LoggerFactory

class HarborConnectorService {
    private val connector = HarborConnector()
    private val logger = LoggerFactory.getLogger("DockerService")

    fun forwardHead(req: HarborConnectorRequest.Head): HarborConnectorResponse {
        logger.info("Service: forwardHead $req")
        return connector.requestHead(req)
    }

    fun forwardManifest(req: HarborConnectorRequest.Manifest): HarborConnectorResponse {
        logger.info("Service: forwardManifest $req")
        return connector.requestManifest(req)
    }

    fun forwardBlob(req: HarborConnectorRequest.Blob): HarborConnectorResponse {
        logger.info("Service: forwardBlob $req")
        return connector.requestBlob(req)
    }
}