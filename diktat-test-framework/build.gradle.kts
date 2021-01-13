plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation(project(":diktat-common"))
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:${Versions.Serialization}")
    implementation("commons-cli:commons-cli:1.4")
    implementation("commons-io:commons-io:2.8.0")
    implementation("org.slf4j:slf4j-api:${Versions.Slf4j}")
    implementation("org.slf4j:slf4j-log4j12:${Versions.Slf4j}")
    implementation("io.github.java-diff-utils:java-diff-utils:4.9")
}

configureJunit()
