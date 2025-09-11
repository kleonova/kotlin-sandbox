package lev.learn.sandbox.order.di

import lev.learn.sandbox.kafkaconnector.KafkaConnector
import lev.learn.sandbox.order.controller.OrderController
import lev.learn.sandbox.order.route.ApiRoute
import lev.learn.sandbox.order.route.OrderRoute
import lev.learn.sandbox.order.service.OrderService
import org.koin.dsl.bind
import org.koin.dsl.module

val orderModule = module {
    single {
        KafkaConnector(
            bootstrapServers = "localhost:9092",
            groupId = "order-service"
        )
    }

    single { OrderService(get()) }
    single { OrderController(get()) }
    factory { OrderRoute(get()) } bind ApiRoute::class
}