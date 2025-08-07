package lev.learn.sandbox.harbor.connector.response

import io.ktor.server.application.ApplicationCall
import io.ktor.utils.io.ByteReadChannel

abstract class DockerResponse {
    abstract fun statusCode(): Int
    abstract fun contentRangeOrNull(): Triple<Long, Long, Long>?
    abstract suspend fun body(): ByteReadChannel
    abstract suspend fun respondTo(call: ApplicationCall)
    open suspend fun discard() {}
}
