package lev.learn.sandbox.order.route

import io.ktor.server.application.Application

interface ApiRoute {
    fun install(application: Application)
}