package com.saveourtool.save.buildutils

import Versions
import org.cqfn.diktat.plugin.gradle.DiktatJavaExecTaskBase

plugins {
    id("org.cqfn.diktat.diktat-gradle-plugin")
}

diktat {
    diktatConfigFile = rootProject.file("diktat-analysis.yml")
    githubActions = findProperty("diktat.githubActions")?.toString()?.toBoolean() ?: false
    inputs {
        // using `Project#path` here, because it must be unique in gradle's project hierarchy
        if (path == rootProject.path) {
            include("gradle/plugins/src/**/*.kt", "*.kts", "gradle/plugins/**/*.kts")
            exclude("gradle/plugins/build/**")
        } else {
            include("src/**/*.kt", "**/*.kts")
            exclude("src/test/**/*.kt", "src/*Test/**/*.kt")
        }
    }
}

tasks.withType<DiktatJavaExecTaskBase>().configureEach {
    javaLauncher.set(project.extensions.getByType<JavaToolchainService>().launcherFor {
        languageVersion.set(JavaLanguageVersion.of(Versions.jdk))
    })
}
