rootProject.name = "kotlin-sandbox"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    resolutionStrategy.eachPlugin {
        if (requested.id.id == "org.jlleitschuh.gradle.ktlint") {
            useVersion(extra["ktlint_version"] as String)
        }
    }
}

dependencyResolutionManagement {
    versionCatalogs {
        create("mylibs") {
            from(files("gradle/libs.versions.toml"))
        }
    }
}

// Модули микросервисов
include("gateway-service")
include("auth-service")
include("notification-service")
include("harbor-connector")
