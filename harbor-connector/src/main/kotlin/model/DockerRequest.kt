package lev.learn.sandbox.harbor.connector.model

sealed class DockerRequest(
    val path: String,
    val headers: List<DockerRequestHeader> = emptyList<DockerRequestHeader>()
) {
    class Head(path: String, headers: List<DockerRequestHeader>) : DockerRequest(path, headers)
    class Manifest(path: String, headers: List<DockerRequestHeader>) : DockerRequest(path, headers)
    class Blob(path: String, headers: List<DockerRequestHeader>) : DockerRequest(path, headers)
}

