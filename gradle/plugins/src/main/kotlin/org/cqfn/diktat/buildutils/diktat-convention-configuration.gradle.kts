package org.cqfn.diktat.buildutils

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
        // a temporary workaround -- diktat-gradle-plugin doesn't detect java version of `javaLauncher`
        languageVersion.set(JavaLanguageVersion.of(11))
    })
}
