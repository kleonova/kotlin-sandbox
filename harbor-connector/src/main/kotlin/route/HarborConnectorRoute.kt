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
import lev.learn.sandbox.harbor.connector.controller.HarborConnectorController
import lev.learn.sandbox.harbor.connector.model.HarborConnectorRequest

private val logger = LoggerFactory.getLogger("HarborConnectorRoute")

fun Route.harborConnectorRoutes() {
    val controller = HarborConnectorController()

    route("/v2/") {
        get {
            logger.info("Route: /v2")
            call.respondText("OK", status = HttpStatusCode.OK)
        }

        head("{path...}") {
            logger.info("Route HEAD: ${call.request.path()}")
            val request = controller.buildRequest(call, "HEAD") as HarborConnectorRequest.Head
            val response = controller.handleHead(request)
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
                is HarborConnectorRequest.Manifest -> controller.handleManifest(req)
                is HarborConnectorRequest.Blob -> controller.handleBlob(req)
                else -> error("Unsupported")
            }



            /*response.response.headers.forEach { key, values ->
                    values.forEach { value ->
                        call.response.headers.append(key, value, safeOnly = false)
                    }
            }*/

            dockerResponse.respondTo(call)
        }
    }
}