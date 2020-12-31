import org.cqfn.diktat.plugin.gradle.DiktatExtension

plugins {
    id("org.cqfn.diktat.diktat-gradle-plugin")
}

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    apply(plugin = "org.cqfn.diktat.diktat-gradle-plugin")
    configure<DiktatExtension> {
        inputs = files("src/**/*.kt")
        debug = true
    }
}
