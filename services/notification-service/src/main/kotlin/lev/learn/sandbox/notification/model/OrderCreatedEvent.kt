package lev.learn.sandbox.notification.model

import java.util.UUID

data class OrderCreatedEvent(
    val id: UUID,
    val customerName: String,
    val customerPhone: String,
    val productName: String,
    val quantity: Int,
    val totalPrice: Double,
)
