plugins {
    id("org.cqfn.diktat.diktat-gradle-plugin") version "0.5.0"
}

repositories {
    mavenCentral()
}

diktat {
    inputs = files("src/**/*.kt")
}
