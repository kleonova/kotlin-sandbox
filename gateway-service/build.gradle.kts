plugins {
    kotlin("jvm")
    application
    id("org.jlleitschuh.gradle.ktlint") // Линтер
}

group = "lev.learn.sandbox.gateway.service"
version = "0.1"

dependencies {
    implementation(mylibs.ktor.server.core)
    implementation(mylibs.ktor.server.netty)
    implementation(mylibs.ktor.server.auth)
    implementation(mylibs.logback)

    testImplementation(mylibs.kotest.runner)
}

application {
    mainClass.set("lev.learn.sandbox.gateway.service.ApplicationKt")
}

// Настройки Ktlint
ktlint {
    verbose.set(true)
}

// Настройки для Kotest
tasks.withType<Test> {
    useJUnitPlatform()
}