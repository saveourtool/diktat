package org.cqfn.diktat.buildutils

import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get

plugins {
    id("org.cqfn.diktat.buildutils.publishing-configuration")
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
