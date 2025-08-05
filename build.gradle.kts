plugins {
    kotlin("jvm") version "2.1.21" apply false
}

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    dependencies {
        // implementation(mylibs.logback)
    }
}