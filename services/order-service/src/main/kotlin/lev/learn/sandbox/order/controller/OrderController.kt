package lev.learn.sandbox.order.controller

import lev.learn.sandbox.order.dto.OrderCreateDto
import lev.learn.sandbox.order.mapper.toOrderCreate
import lev.learn.sandbox.order.service.OrderService

class OrderController(
    private val service: OrderService
) {
    fun createOrder(order: OrderCreateDto) {
        service.createOrder(order.toOrderCreate())
    }
}