plugins {
    id("org.cqfn.diktat.diktat-gradle-plugin") version "0.1.6"
}

repositories {
    mavenCentral()
}

diktat {
    debug = true
    inputs = files("src/**/*.kt")
}
