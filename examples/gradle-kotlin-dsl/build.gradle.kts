plugins {
    id("org.cqfn.diktat.diktat-gradle-plugin") version "1.0.0-rc.3"
}

repositories {
    mavenCentral()
}

diktat {
    inputs = files("src/**/*.kt")
}
