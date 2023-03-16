package org.cqfn.diktat.buildutils

import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = freeCompilerArgs + "-opt-in=kotlin.RequiresOptIn"
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

configureJacoco()
tasks.withType<Test> {
    useJUnitPlatform()
}
