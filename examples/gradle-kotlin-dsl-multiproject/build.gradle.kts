import com.saveourtool.diktat.plugin.gradle.DiktatExtension

plugins {
    kotlin("jvm") version "1.9.21"
    id("com.saveourtool.diktat")
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    apply(plugin = "com.saveourtool.diktat")
    configure<DiktatExtension> {
        diktatConfigFile = rootProject.file("diktat-analysis.yml")
        inputs { include("src/**/*.kt") }
        debug = true
    }
}
