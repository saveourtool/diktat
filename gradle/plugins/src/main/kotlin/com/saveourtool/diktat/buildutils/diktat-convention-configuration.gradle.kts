package com.saveourtool.diktat.buildutils

plugins {
    id("com.saveourtool.diktat")
}

diktat {
    diktatConfigFile = rootProject.file("diktat-analysis.yml")
    // githubActions = findProperty("diktat.githubActions")?.toString()?.toBoolean() ?: false

    reporters {
        json()
        githubActions()
    }

    // reporters {
    // directory = project.rootProject.layout.buildDirectory.file("reports/diktat")
    // json {
    // output = "report.json"
    // }
    // sarif {
    // mergeOutput = ""
    // }
    // custom {
    // dependency = "bla.bla:foo.foo:1.2.3"
    // output = "report.txt"
    // }
    // }
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
