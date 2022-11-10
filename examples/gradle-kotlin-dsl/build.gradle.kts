plugins {
    id("org.cqfn.diktat.diktat-gradle-plugin") version "1.2.4"
}

repositories {
    mavenCentral()
}

diktat {
    inputs { include("src/**/*.kt") }
}
