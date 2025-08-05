plugins {
    kotlin("jvm")
    application
    id("org.jlleitschuh.gradle.ktlint") // Линтер
}

group = "lev.learn.sandbox.harbor.connector"
version = "unspecified"

dependencies {
    implementation(mylibs.ktor.server.core)
    implementation(mylibs.ktor.server.netty)
    implementation(mylibs.ktor.server.call.logging)
    implementation(mylibs.ktor.server.auth)
    implementation(mylibs.ktor.client.core)
    implementation(mylibs.ktor.client.cio)
    implementation(mylibs.ktor.client.auth)
    implementation(mylibs.logback)

    testImplementation(mylibs.kotest.runner)
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

// Настройки Ktlint
ktlint {
    verbose.set(true)
}

// Настройки для Kotest
tasks.withType<Test> {
    useJUnitPlatform()
}