package lev.learn.sandbox.harbor.connector.model

import io.ktor.http.HttpHeaders

sealed class DockerRequest(
    open val path: String,
    open val headers: List<DockerRequestHeader> = emptyList()
) {
    data class Head(
        override val path: String,
        override val headers: List<DockerRequestHeader> = emptyList()
    ) : DockerRequest(path, headers)

    data class Manifest(
        override val path: String,
        override val headers: List<DockerRequestHeader> = emptyList()
    ) : DockerRequest(path, headers)

    data class Blob(
        override val path: String,
        override val headers: List<DockerRequestHeader> = emptyList()
    ) : DockerRequest(path, headers.filterNot { it.key == HttpHeaders.AcceptEncoding }) {
        fun addHeader(header: DockerRequestHeader): Blob {
            return copy(headers = headers + header)
        }
    }
}


