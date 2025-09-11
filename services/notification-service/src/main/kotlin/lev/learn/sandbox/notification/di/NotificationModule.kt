package lev.learn.sandbox.notification.di

import lev.learn.sandbox.kafkaconnector.KafkaConnector
import lev.learn.sandbox.notification.controller.EventController
import lev.learn.sandbox.notification.controller.NotificationController
import lev.learn.sandbox.notification.route.ApiRoute
import lev.learn.sandbox.notification.route.NotificationRoute
import lev.learn.sandbox.notification.service.EmailService
import lev.learn.sandbox.notification.service.EventService
import org.koin.dsl.bind
import org.koin.dsl.module

val notificationModule = module {
    single {
        KafkaConnector(
            bootstrapServers = "localhost:9092",
            groupId = "notification-service"
        )
    }

    single { EmailService() }
    single { EventService(get(), get()) }

    single { EventController(get()) }
    single { NotificationController(get()) }

    factory { NotificationRoute(get()) } bind ApiRoute::class
}
