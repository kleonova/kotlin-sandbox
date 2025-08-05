package lev.learn.sandbox.harbor.connector.model

sealed class HarborConnectorRequest(val path: String) {
    class Head(path: String) : HarborConnectorRequest(path)
    class Manifest(path: String) : HarborConnectorRequest(path)
    class Blob(path: String) : HarborConnectorRequest(path)
}