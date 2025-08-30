plugins {
    application
//    alias(libs.plugins.serialization)
//    alias(libs.plugins.shadow)
}

group = "lev.learn.sandbox.notification.service"
version = "0.1"

application {
    mainClass.set("lev.learn.sandbox.notification.service.ApplicationKt")
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.server.status.pages)

    implementation(libs.serialization.json)

    implementation("com.sun.mail:jakarta.mail:2.0.1")
    // implementation("com.icegreen:greenmail-junit5:2.0.1")

    testImplementation(libs.kotest.runner)
}
