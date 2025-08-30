package lev.learn.sandbox.auth.service

import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import lev.learn.sandbox.auth.service.database.DatabaseFactory
import lev.learn.sandbox.auth.service.route.authRoutes
import lev.learn.sandbox.auth.service.security.KeycloakSettings
import lev.learn.sandbox.auth.service.security.configureKeycloakAuth

fun main() {
    val config = ApplicationConfig("application.conf")
    val port = config.property("ktor.deployment.port").getString().toInt()

    embeddedServer(Netty, port = port, module = {
        module(config)
    }).start(wait = true)
}

fun Application.module(config: ApplicationConfig) {
    // Запускаем миграции
    DatabaseFactory.init(config)
    val keycloak = KeycloakSettings.create(config)

    log.info("Auth Service is running!")

    // подключаем Keycloak
    configureKeycloakAuth(keycloak)

    // Роутинг
    routing {
        get("/") {
            call.respondText("Auth Service is running!")
        }

        authRoutes(keycloak)
    }
}
