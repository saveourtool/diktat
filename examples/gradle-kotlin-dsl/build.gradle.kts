plugins {
    id("org.cqfn.diktat.diktat-gradle-plugin") version "0.1.6"
}

repositories {
    mavenCentral()
}

diktat {
    inputs = files("src/**/*.kt")
}
