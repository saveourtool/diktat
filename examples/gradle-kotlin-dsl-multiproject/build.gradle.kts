import com.saveourtool.diktat.plugin.gradle.DiktatExtension

plugins {
    kotlin("jvm") version "1.9.10"
    id("com.saveourtool.diktat.diktat-gradle-plugin")
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    apply(plugin = "com.saveourtool.diktat.diktat-gradle-plugin")
    configure<DiktatExtension> {
        diktatConfigFile = rootProject.file("diktat-analysis.yml")
        inputs { include("src/**/*.kt") }
        debug = true
    }
}
