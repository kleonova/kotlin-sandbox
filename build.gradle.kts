import io.gitlab.arturbosch.detekt.extensions.DetektExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.ktlint) apply false
}

group = "lev.learn.sandbox"
version = "0.1.0"

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "io.gitlab.arturbosch.detekt")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    dependencies {
        "implementation"(kotlin("stdlib"))
        "implementation"(project.rootProject.libs.logback)

        // Тесты
        "testImplementation"(kotlin("test"))
        "testImplementation"(project.rootProject.libs.kotest.runner)
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }

    // Настройка Detekt через расширение
    extensions.configure<DetektExtension> {
        toolVersion = project.rootProject.libs.versions.detekt.get()
        config.setFrom(files("$rootDir/config/detekt/detekt.yml"))
        // применяет файлы конфигурации поверх файла конфигурации, по умолчанию false
        buildUponDefaultConfig = true
        // включает все правила, по умолчанию false
        allRules = false
        // true - сборка не завершается ошибкой при возникновении каких-либо проблем, по умолчанию false
        ignoreFailures = true
    }

    // Настройки ktlint
    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        debug.set(true)
        verbose.set(true)
        android.set(false)
        outputToConsole.set(true)
        ignoreFailures.set(true)
        enableExperimentalRules.set(true)
    }
}
