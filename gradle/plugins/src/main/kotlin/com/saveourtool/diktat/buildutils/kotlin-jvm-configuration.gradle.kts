package com.saveourtool.diktat.buildutils

import org.gradle.api.tasks.testing.Test

plugins {
    kotlin("jvm")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.jdk))
    }
}

kotlin {
    compilerOptions {
        optIn.add("kotlin.RequiresOptIn")
    }

    jvmToolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.jdk))
    }
}

tasks.register<Jar>(SOURCES_JAR) {
    archiveClassifier.set("sources")
    from(kotlin.sourceSets.main.map { it.kotlin })
}

configureJacoco()
tasks.withType<Test> {
    useJUnitPlatform()
}
