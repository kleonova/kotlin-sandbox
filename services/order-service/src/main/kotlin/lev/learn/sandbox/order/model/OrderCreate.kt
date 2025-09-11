package lev.learn.sandbox.order.model

import java.util.UUID

data class OrderCreate(
    val id: UUID,
    val customerName: String,
    val customerPhone: String,
    val productName: String,
    val quantity: Int,
    val totalPrice: Double,
)
