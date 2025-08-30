package lev.learn.sandbox.harbor.connector

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.routing.*
import io.ktor.server.response.*
import lev.learn.sandbox.harbor.connector.route.harborConnectorRoutes

fun main() {
    val config = ApplicationConfig("application.conf")
    val port = config.property("ktor.deployment.port").getString().toInt()

    val serviceName = config.property("name").getString()

    embeddedServer(Netty, port = port) {
        install(CallLogging)

        routing {
            get("/") {
                call.respondText("$serviceName is running on $port!")
            }

            harborConnectorRoutes()
        }
    }.start(wait = true)
}
