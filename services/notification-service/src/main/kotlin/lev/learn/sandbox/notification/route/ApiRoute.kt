package lev.learn.sandbox.notification.route

import io.ktor.server.application.Application

interface ApiRoute {
    fun install(application: Application)
}
