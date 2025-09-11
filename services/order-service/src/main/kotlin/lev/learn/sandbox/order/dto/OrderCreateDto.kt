package lev.learn.sandbox.order.dto

import kotlinx.serialization.Serializable

@Serializable
data class OrderCreateDto(
    val customerName: String,
    val customerPhone: String,
    val productName: String,
    val quantity: Int,
    val totalPrice: Double,
)
