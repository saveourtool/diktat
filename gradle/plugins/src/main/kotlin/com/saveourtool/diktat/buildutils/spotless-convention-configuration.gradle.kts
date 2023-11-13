package com.saveourtool.diktat.buildutils

plugins {
    id("com.diffplug.spotless")
}

spotless {
    kotlin {
        target("**/*.kt")

        ktlint()
        diktat().configFile(
            rootProject.file("diktat-analysis-old.yml")
        )
    }
}

