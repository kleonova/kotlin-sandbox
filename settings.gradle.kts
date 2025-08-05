rootProject.name = "kotlin-sandbox"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }

    resolutionStrategy.eachPlugin {
        if (requested.id.id == "org.jlleitschuh.gradle.ktlint") {
            useVersion(extra["ktlintVersion"] as String)
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

include("harbor-connector")
include("file-receiver")
