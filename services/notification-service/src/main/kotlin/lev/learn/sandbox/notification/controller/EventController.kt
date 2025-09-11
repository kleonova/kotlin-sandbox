package lev.learn.sandbox.notification.controller

import lev.learn.sandbox.notification.service.EventService

class EventController(
    private val service: EventService
) {
    fun start() {
        service.startConsuming()
    }
}
