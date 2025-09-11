plugins {
    application
    alias(libs.plugins.kotlin.serialization)
}

group = "lev.learn.sandbox.notification.service"
version = "0.1"

application {
    mainClass.set("lev.learn.sandbox.notification.service.ApplicationKt")
}

dependencies {
    // project module
    implementation(project(":connector:kafka-connector"))

    // Koin
    implementation(libs.koin.ktor)
    implementation(libs.koin.core)

    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)
    implementation(libs.ktor.serialization.json)

    implementation(libs.serialization.json)

    implementation(libs.mail)

    testImplementation(libs.kotest.runner)
}
