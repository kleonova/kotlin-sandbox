package lev.learn.sandbox.notification.service

import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import lev.learn.sandbox.notification.service.route.configureNotificationRouting

fun main() {
    val config = ApplicationConfig("application.conf")
    val port = config.property("ktor.deployment.port").getString().toInt()

    embeddedServer(Netty, port = port) {
        routing {
            get("/") {
                call.respondText("Notification Service is running!")
            }
        }

        configureNotificationRouting()
    }.start(wait = true)
}
