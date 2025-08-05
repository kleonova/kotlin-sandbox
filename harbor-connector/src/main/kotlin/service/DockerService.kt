package lev.learn.sandbox.harbor.connector.service

import lev.learn.sandbox.harbor.connector.connector.HarborConnector
import lev.learn.sandbox.harbor.connector.model.DockerRequest
import lev.learn.sandbox.harbor.connector.model.DockerResponse
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
        logger.info("Service: forwardBlob $req")
        return connector.requestBlob(req)
    }
}