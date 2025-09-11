package lev.learn.sandbox.order.route

import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import lev.learn.sandbox.order.controller.OrderController
import lev.learn.sandbox.order.dto.OrderCreateDto

class OrderRoute(
    private val controller: OrderController
) : ApiRoute {
    override fun install(application: Application) {
        application.routing {
            route("/api/v1/orders") {
                post {
                    val order = call.receive<OrderCreateDto>()
                    controller.createOrder(order)
                    call.respondText("Order created successfully!")
                }
            }
        }
    }
}