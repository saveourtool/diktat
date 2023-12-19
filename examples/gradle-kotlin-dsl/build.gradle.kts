plugins {
    id("com.saveourtool.diktat") version "2.0.0"
}

repositories {
    mavenCentral()
}

diktat {
    inputs { include("src/**/*.kt") }
}
