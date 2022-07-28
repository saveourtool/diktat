plugins {
    id("org.cqfn.diktat.diktat-gradle-plugin") version "1.2.2"
}

repositories {
    mavenCentral()
}

diktat {
    inputs { include("src/**/*.kt") }
}
