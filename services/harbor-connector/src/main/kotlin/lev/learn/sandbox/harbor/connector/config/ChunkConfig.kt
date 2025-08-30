package lev.learn.sandbox.harbor.connector.config

data class ChunkConfig(
    val prefetchCount: Int = DEFAULT_PREFETCH_COUNT,
    val sizeBytes: Long = DEFAULT_SIZE_BYTES,
) {
    companion object {
        const val DEFAULT_PREFETCH_COUNT = 3
        const val DEFAULT_SIZE_BYTES = 100L * 1024 * 1024
    }
}
