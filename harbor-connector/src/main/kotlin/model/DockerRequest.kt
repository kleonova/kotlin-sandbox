package lev.learn.sandbox.harbor.connector.model

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
    ) : DockerRequest(path, headers)
}


