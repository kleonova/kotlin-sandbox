plugins {
    application
    // kotlin("plugin.serialization")
}

group = "lev.learn.sandbox.auth.service"
version = "0.1"

application {
    mainClass.set("lev.learn.sandbox.auth.service.ApplicationKt")
}

dependencies {
    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.server.call.logging)

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

    // Тесты
    testImplementation(libs.kotest.runner)
}
