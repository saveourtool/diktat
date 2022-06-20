plugins {
    id("org.cqfn.diktat.diktat-gradle-plugin") version "1.1.0"
    kotlin("jvm") version "1.7.0"
}

repositories {
    mavenCentral()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += listOf("-Xuse-k2")
    }
}

diktat {
    inputs { include("src/**/*.kt") }
}
