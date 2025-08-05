package lev.learn.sandbox.harbor.connector

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import lev.learn.sandbox.harbor.connector.route.harborConnectorRoutes

fun main() {
    val config = ApplicationConfig("application.conf")
    val port = config.property("ktor.deployment.port").getString().toInt()

    embeddedServer(Netty, port = port) {
        routing {
            get("/") {
                call.respondText("Harbor Connector Service is running!")
            }

            harborConnectorRoutes()
        }
    }.start(wait = true)
}