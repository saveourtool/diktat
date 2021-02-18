plugins {
    id("org.cqfn.diktat.diktat-gradle-plugin") version "0.4.1"
}

repositories {
    mavenCentral()
}

diktat {
    inputs = files("src/**/*.kt")
}
