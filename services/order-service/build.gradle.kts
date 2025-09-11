plugins {
    application
}

group = "lev.learn.sandbox.order"
version = "0.1.0"

application {
    mainClass.set("lev.learn.sandbox.order.ApplicationKt")
}

dependencies {
    // project module
    implementation(project(":connector:kafka-connector"))

    // Kotlin
    implementation(libs.kotlinx.coroutines)

    // Koin
    implementation(libs.koin.ktor)
    implementation(libs.koin.core)

    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.server.status.pages)

    // PostgreSQL
    implementation(libs.postgres)

    // HikariCP
    implementation(libs.hikari)

    // Exposed
    implementation(libs.exposed.core)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.java.time)

    // Flyway
    implementation(libs.flyway)
    implementation(libs.flyway.postgresql)

    // Serialization
    implementation(libs.serialization.json)
}
