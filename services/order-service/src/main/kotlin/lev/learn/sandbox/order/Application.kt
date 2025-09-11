package lev.learn.sandbox.order

import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.request.path
import lev.learn.sandbox.order.route.ApiRoute
import org.koin.ktor.plugin.koin
import io.ktor.server.application.log
import lev.learn.sandbox.order.di.orderModule
import lev.learn.sandbox.order.utils.configureErrorHandling
import org.koin.ktor.plugin.Koin

fun main() {
    val config = ApplicationConfig("application.conf")
    val port = config.property("ktor.deployment.port").getString().toInt()

    embeddedServer(Netty, port = port, module = {
        module(config)
    }).start(wait = true)
}

fun Application.module(config: ApplicationConfig) {
    install(Koin) {
        modules(
            orderModule
        )
    }

    install(CallLogging) {
        level = org.slf4j.event.Level.INFO
        filter { call -> call.request.path().startsWith("/api/") }
    }

    install(ContentNegotiation) {
        jackson {
            configure(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
        }
    }

//    DatabaseFactory.init(config)

    // глобальный обработчик ошибок
    configureErrorHandling()

    // routing
    val routes: List<ApiRoute> = koin().getAll<ApiRoute>()
    routes.forEach { route ->
        route.install(this)
    }

    log.info("Catalog Service is running!")
}
