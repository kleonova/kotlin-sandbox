package lev.learn.sandbox.harbor.connector.model

import io.ktor.utils.io.ByteReadChannel

abstract class DockerResponse {
    abstract fun statusCode(): Int
    abstract fun contentRangeOrNull(): Triple<Long, Long, Long>?
    abstract suspend fun body(): ByteReadChannel
    abstract suspend fun bodyAsChannel(): ByteReadChannel
    open suspend fun discard() {}
}
