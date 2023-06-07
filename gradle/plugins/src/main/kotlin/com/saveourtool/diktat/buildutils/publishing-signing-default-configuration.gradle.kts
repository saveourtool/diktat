package com.saveourtool.diktat.buildutils

import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

plugins {
    id("com.saveourtool.diktat.buildutils.publishing-configuration")
}

run {
    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["java"])
            }
        }
    }

    configureSigning()
}
