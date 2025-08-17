plugins {
    kotlin("jvm")
    application
    alias(libs.plugins.serialization)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.shadow)
}

group = "lev.learn.sandbox.notification.service"
version = "0.1"

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.serialization.json)
    implementation(libs.logback)

    implementation(libs.serialization.json)

    implementation("com.sun.mail:jakarta.mail:2.0.1")
    implementation("com.icegreen:greenmail-junit5:2.0.1") // можно, но мы используем MailHog

    testImplementation(libs.kotest.runner)
}

application {
    mainClass.set("lev.learn.sandbox.notification.service.ApplicationKt")
}

// Настройки Ktlint
ktlint {
    verbose.set(true)
}

// Настройки для Kotest
tasks.withType<Test> {
    useJUnitPlatform()
}