plugins {
    kotlin("jvm")
    application
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadow)
}

group = "lev.learn.sandbox.auth.service"
version = "0.1"

dependencies {
    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.serialization.kotlinx.json)

    // Logging
    implementation(libs.logback)

    // PostgreSQL
    implementation(libs.postgres)

    // HikariCP
    implementation(libs.hikari)

    // Flyway
    implementation(libs.flyway)
    implementation(libs.flyway.postgresql)

    // Тесты
    testImplementation(libs.kotest.runner)
    testImplementation(libs.ktor.server.test)
}

application {
    mainClass.set("lev.learn.sandbox.auth.service.ApplicationKt")
}
