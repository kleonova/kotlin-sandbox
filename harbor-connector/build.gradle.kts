plugins {
    application
}

group = "lev.learn.sandbox.harbor.connector"
version = "1.0"

application {
    mainClass.set("lev.learn.sandbox.harbor.connector.ApplicationKt")
}

dependencies {
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.call.logging)
    implementation(libs.ktor.server.auth)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.auth)
    // implementation(libs.typesafe.config)
}
