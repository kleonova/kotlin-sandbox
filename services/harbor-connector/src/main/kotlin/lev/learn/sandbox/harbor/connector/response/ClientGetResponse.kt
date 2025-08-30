package lev.learn.sandbox.harbor.connector.response

import io.ktor.http.Headers
import io.ktor.utils.io.ByteReadChannel

data class ClientGetResponse(val channel: ByteReadChannel, val headers: Headers)
