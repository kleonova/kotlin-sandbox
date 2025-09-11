package lev.learn.sandbox.order.mapper

import lev.learn.sandbox.order.dto.OrderCreateDto
import lev.learn.sandbox.order.model.OrderCreate
import java.util.UUID

fun OrderCreateDto.toOrderCreate(): OrderCreate {
    return OrderCreate(
        id = UUID.randomUUID(),
        customerName = customerName,
        customerPhone = customerPhone,
        productName = productName,
        quantity = quantity,
        totalPrice = totalPrice,
    )
}