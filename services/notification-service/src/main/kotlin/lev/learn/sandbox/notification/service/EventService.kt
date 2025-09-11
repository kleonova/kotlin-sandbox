package lev.learn.sandbox.notification.service

import lev.learn.sandbox.kafkaconnector.KafkaConnector
import lev.learn.sandbox.notification.model.EmailNotification
import lev.learn.sandbox.notification.model.OrderCreatedEvent

class EventService(
    private val kafkaConnector: KafkaConnector,
    private val emailService: EmailService
) {
    fun startConsuming() {
        kafkaConnector.subscribe("order_create", OrderCreatedEvent::class.java) { event ->
            handleOrderCreated(event)
        }
    }

    private fun handleOrderCreated(event: OrderCreatedEvent) {
        emailService.sendEmail(
            EmailNotification(
                to = "some@customer.ru",
                subject = "Order create",
                body = event.toString()
            )
        )
    }
}
