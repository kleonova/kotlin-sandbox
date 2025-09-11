package lev.learn.sandbox.order.service

import lev.learn.sandbox.kafkaconnector.KafkaConnector
import lev.learn.sandbox.order.model.OrderCreate

class OrderService(
    private val kafka: KafkaConnector
) {
    fun createOrder(order: OrderCreate) {
        // save to db
        println("Order saved: ${order.id}")

        // send event to kafka
        kafka.send(
            topic = "order_create",
            key = order.customerName,
            message = order
        )
    }
}