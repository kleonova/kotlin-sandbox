package lev.learn.sandbox.harbor.connector.route

import org.slf4j.LoggerFactory
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.head
import io.ktor.server.routing.route
import io.ktor.utils.io.ByteWriteChannel
import io.ktor.utils.io.copyAndClose
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

                controller.handleStreamBlob(request as DockerRequest.Blob) { response ->
                    call.respond(object : OutgoingContent.WriteChannelContent() {
                        override val contentType = ContentType.Application.OctetStream
                        override val headers = response.headers

                        override suspend fun writeTo(channel: ByteWriteChannel) {
                            response.channel.copyAndClose(channel)
                        }
                    })
                }
            }
        }

        get("/v2/{repository}/manifests/{reference}") {
            logger.info("Route GET manifest: ${call.request.path()}")
            call.respond(HttpStatusCode.OK)
        }
    }
}
