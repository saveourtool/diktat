/**
 * Configuration for code coverage calculation via Jacoco
 */

package com.saveourtool.save.buildutils

import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.named
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoReport

/**
 * Configure jacoco for [this] project
 */
fun Project.configureJacoco() {
    apply<JacocoPlugin>()

    configure<JacocoPluginExtension> {
        toolVersion = "0.8.7"
    }

    tasks.named<Test>("test") {
        finalizedBy("jacocoTestReport")
    }
    tasks.named<JacocoReport>("jacocoTestReport") {
        dependsOn(tasks.named<Test>("test"))
        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}
