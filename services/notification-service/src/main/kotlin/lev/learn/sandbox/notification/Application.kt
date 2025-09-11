package lev.learn.sandbox.notification

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import kotlinx.serialization.json.Json
import lev.learn.sandbox.notification.controller.EventController
import lev.learn.sandbox.notification.di.notificationModule
import lev.learn.sandbox.notification.route.ApiRoute
import org.koin.ktor.plugin.Koin
import org.koin.ktor.plugin.koin
import kotlin.collections.forEach

fun main() {
    val config = ApplicationConfig("application.conf")
    val port = config.property("ktor.deployment.port").getString().toInt()

    embeddedServer(Netty, port = port, module = {
        module(port)
    }).start(wait = true)
}

fun Application.module(port: Int) {
    install(Koin) {
        modules(
            notificationModule
        )
    }

    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
        }) // Подключаем поддержку JSON
    }

    val controller = koin().get<EventController>()
    controller.start()  // подписка на Kafka

    // routing
    val routes: List<ApiRoute> = koin().getAll<ApiRoute>()
    routes.forEach { route ->
        route.install(this)
    }

    log.info("Catalog Service is running on $port!")
}
