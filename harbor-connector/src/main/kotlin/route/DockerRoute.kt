package lev.learn.sandbox.harbor.connector.route

import org.slf4j.LoggerFactory
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytesWriter
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.head
import io.ktor.server.routing.route
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

            if (path.contains("/manifests/")) {
                val request = controller.buildRequest(call, "GET_MANIFEST")
                val dockerResponse = controller.handleManifest(request as DockerRequest.Manifest)
                dockerResponse.respondTo(call)
            } else {
                val request = controller.buildRequest(call, "GET_BLOB")

                call.respondBytesWriter(contentType = ContentType.Application.OctetStream) {
                    logger.info("Route → начинаем писать клиенту")
                    controller.handleStreamBlob(request as DockerRequest.Blob) { response ->
                        logger.info("Route → получили response, начинаем копирование")

                        val buffer = ByteArray(8192)
                        var read: Int
                        while (true) {
                            read = response.channel.readAvailable(buffer, 0, buffer.size)
                            if (read <= 0) break
                            writeFully(buffer, 0, read)
                        }
                        logger.info("Route → копирование завершено")
                    }
                }
            }
        }

        get("/v2/{repository}/manifests/{reference}") {
            logger.info("Route GET manifest: ${call.request.path()}")
            call.respond(HttpStatusCode.OK)
        }
    }
}
