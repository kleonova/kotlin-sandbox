group = "lev.learn.sandbox.kafkaconnector"
version = "0.1.0"

dependencies {
    implementation(libs.kafka.clients)
    implementation(libs.serialization.jackson)
    implementation(libs.serialization.json)
    implementation(libs.logback)
}
