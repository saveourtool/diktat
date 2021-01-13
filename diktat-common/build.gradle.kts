plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:${Versions.Serialization}")
    implementation("com.charleskorn.kaml:kaml:0.26.0")
    implementation("commons-cli:commons-cli:1.4")
    implementation("org.slf4j:slf4j-api:${Versions.Slf4j}")
    implementation("org.slf4j:slf4j-log4j12:${Versions.Slf4j}")
}

configureJunit()
