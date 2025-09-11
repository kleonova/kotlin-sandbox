package lev.learn.sandbox.notification.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import lev.learn.sandbox.notification.controller.NotificationController
import lev.learn.sandbox.notification.model.NotificationRequest

class NotificationRoute(
    private val controller: NotificationController
) : ApiRoute {
    override fun install(application: Application) {
        application.routing {
            route("/api/v1/notifications") {
                post("/notify") {
                    val request = call.receive<NotificationRequest>()
                    controller.sendEmail(request)
                    call.respond(HttpStatusCode.Accepted, "Email sent to ${request.to}")
                }
            }
        }
    }
}
