package com.saveourtool.diktat.buildutils

import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs +
                "-opt-in=kotlin.RequiresOptIn" + "-Werror"
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(Versions.jdk))
    }
}

kotlin {
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
