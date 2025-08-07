package lev.learn.sandbox.harbor.connector.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.path
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.head
import io.ktor.server.routing.route
import org.slf4j.LoggerFactory
import lev.learn.sandbox.harbor.connector.controller.DockerController
import lev.learn.sandbox.harbor.connector.model.DockerRequest
import lev.learn.sandbox.harbor.connector.response.DockerResponseBase

private val logger = LoggerFactory.getLogger("HarborConnectorRoute")

fun Route.harborConnectorRoutes() {
    val controller = DockerController()

    route("/v2/") {
        get {
            logger.info("Route: /v2")
            call.respondText("OK", status = HttpStatusCode.OK)
        }

        head("{path...}") {
            logger.info("Route HEAD: ${call.request.path()}")
            val request = controller.buildRequest(call, "HEAD") as DockerRequest.Head
            val response = controller.handleHead(request) as DockerResponseBase
            response.respondTo(call)
        }

        get("{path...}") {
            logger.info("Route GET: ${call.request.path()}")

            val path = call.parameters.getAll("path")?.joinToString("/") ?: ""
            val req = if (path.contains("/manifests/")) {
                controller.buildRequest(call, "GET_MANIFEST")
            } else {
                controller.buildRequest(call, "GET_BLOB")
            }

            val dockerResponse = when (req) {
                is DockerRequest.Manifest -> controller.handleManifest(req)
                is DockerRequest.Blob -> controller.handleBlob(req)
                else -> error("Unsupported")
            }

            dockerResponse.respondTo(call)
        }
    }
}