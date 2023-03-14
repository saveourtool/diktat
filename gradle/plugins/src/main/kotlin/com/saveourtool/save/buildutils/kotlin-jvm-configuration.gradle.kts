package com.saveourtool.save.buildutils

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

kotlin {
    jvmToolchain {
        this.languageVersion.set(JavaLanguageVersion.of(Versions.jdk))
    }
}

configureJacoco()
tasks.withType<Test> {
    useJUnitPlatform()
}
