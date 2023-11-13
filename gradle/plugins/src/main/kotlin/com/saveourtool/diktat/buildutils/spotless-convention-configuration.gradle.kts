package com.saveourtool.diktat.buildutils

plugins {
    id("com.diffplug.spotless")
}

spotless {
    kotlin {
        // using `Project#path` here, because it must be unique in gradle's project hierarchy
        if (path == rootProject.path) {
            target("gradle/plugins/src/**/*.kt", "*.kts", "gradle/plugins/**/*.kts")
            targetExclude("gradle/plugins/build/**")
        } else {
            target("src/**/*.kt", "**/*.kts")
            targetExclude(
                "src/test/**/*.kt",
                "src/test/**/*.kts",
                "src/*Test/**/*.kt",
                "build/**/*.kts",
            )
        }
        diktat().configFile(
            rootProject.file("diktat-analysis-old.yml")
        )
    }
}

