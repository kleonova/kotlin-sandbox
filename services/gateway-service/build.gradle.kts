plugins {
    application
}

group = "lev.learn.sandbox.gateway.service"
version = "0.1"

application {
    mainClass.set("lev.learn.sandbox.gateway.service.ApplicationKt")
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.auth)
}
