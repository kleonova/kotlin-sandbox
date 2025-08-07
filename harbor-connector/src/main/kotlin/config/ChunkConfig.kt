package lev.learn.sandbox.harbor.connector.config

data class ChunkConfig(
    val prefetchCount: Int = 3,
    val sizeBytes: Long = 100L * 1024 * 1024,
)
