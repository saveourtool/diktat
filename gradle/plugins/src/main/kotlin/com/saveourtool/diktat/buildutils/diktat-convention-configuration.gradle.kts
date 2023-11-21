package com.saveourtool.diktat.buildutils

plugins {
    id("com.saveourtool.diktat")
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
            exclude(
                "src/test/**/*.kt",
                "src/test/**/*.kts",
                "src/*Test/**/*.kt",
                "build/**/*.kts",
            )
        }
    }
}
