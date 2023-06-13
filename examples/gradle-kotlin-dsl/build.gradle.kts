plugins {
    id("com.saveourtool.diktat.diktat-gradle-plugin") version "1.2.5"
}

repositories {
    mavenCentral()
}

diktat {
    inputs { include("src/**/*.kt") }
}
