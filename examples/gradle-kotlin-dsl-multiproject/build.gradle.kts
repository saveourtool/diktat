import org.cqfn.diktat.plugin.gradle.DiktatExtension

plugins {
    kotlin("jvm") version "1.7.10"
    id("org.cqfn.diktat.diktat-gradle-plugin")
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    apply(plugin = "org.cqfn.diktat.diktat-gradle-plugin")
    configure<DiktatExtension> {
        diktatConfigFile = rootProject.file("diktat-analysis.yml")
        inputs { include("src/**/*.kt") }
        debug = true
    }
}
