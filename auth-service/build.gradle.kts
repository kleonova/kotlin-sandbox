plugins {
    kotlin("jvm")
    application
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadow)
    alias(libs.plugins.cyclonedx.bom)
}

group = "lev.learn.sandbox.auth.service"
version = "0.1"

dependencies {
    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.jackson)
    implementation(libs.ktor.serialization.json)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.serialization.json)

    // Logging
    implementation(libs.logback)

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

    // Тесты
    testImplementation(libs.kotest.runner)
    testImplementation(libs.ktor.server.test)
}

application {
    mainClass.set("lev.learn.sandbox.auth.service.ApplicationKt")
}

tasks.cyclonedxBom {
    setProjectType("application")
    setOutputFormat("json")
    setOutputName("kotlin-sandbox-auth-service")
    setIncludeConfigs(listOf("runtimeClasspath"))
}