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
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.server.call.logging)

    // Тесты
    testImplementation(libs.kotest.runner)
}

application {
    mainClass.set("lev.learn.sandbox.auth.service.ApplicationKt")
}
