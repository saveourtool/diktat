package org.cqfn.diktat.buildutils

import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.getValue
import org.gradle.kotlin.dsl.kotlin
import org.gradle.kotlin.dsl.provideDelegate
import org.gradle.kotlin.dsl.registering
import org.gradle.kotlin.dsl.the
import java.io.File

plugins {
    kotlin("jvm")
}

val ktlintVersion: String = the<LibrariesForLibs>()
    .versions
    .ktlint
    .get()

val generateVersionsFile by tasks.registering {
    val outputDir = File("$buildDir/generated/src")
    val versionsFile = outputDir.resolve("generated/Versions.kt")

    val diktatVersion = version.toString()

    inputs.property("diktat version", diktatVersion)
    inputs.property("ktlint version", ktlintVersion)
    outputs.dir(outputDir)

    doFirst {
        versionsFile.parentFile.mkdirs()
        versionsFile.writeText(
            """
            package generated

            internal const val DIKTAT_VERSION = "$diktatVersion"
            internal const val KTLINT_VERSION = "$ktlintVersion"

            """.trimIndent()
        )
    }
}

kotlin.sourceSets.getByName("main") {
    kotlin.srcDir(
        generateVersionsFile.map {
            it.outputs.files.singleFile
        }
    )
}
