/**
 * Utilities for diktat gradle plugin
 */

@file:Suppress("FILE_NAME_MATCH_CLASS", "MatchingDeclarationName")

package com.saveourtool.diktat.plugin.gradle

import groovy.lang.Closure
import org.gradle.api.Project
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

@Suppress(
    "MISSING_KDOC_TOP_LEVEL",
    "MISSING_KDOC_CLASS_ELEMENTS",
    "KDOC_NO_CONSTRUCTOR_PROPERTY",
    "MISSING_KDOC_ON_FUNCTION",
    "KDOC_WITHOUT_PARAM_TAG",
    "KDOC_WITHOUT_RETURN_TAG"
)
class KotlinClosure1<in T : Any?, V : Any>(
    val function: T.() -> V?,
    owner: Any? = null,
    thisObject: Any? = null
) : Closure<V?>(owner, thisObject) {
    @Suppress("unused")  // to be called dynamically by Groovy
    fun doCall(it: T): V? = it.function()
}

// These two are copy-pasted from `kotlin-dsl` plugin's groovy interop.
// Because `kotlin-dsl` depends on kotlin 1.3.x.
@Suppress(
    "MISSING_KDOC_TOP_LEVEL",
    "MISSING_KDOC_ON_FUNCTION",
    "KDOC_WITHOUT_PARAM_TAG",
    "KDOC_WITHOUT_RETURN_TAG"
)
fun <T> Any.closureOf(action: T.() -> Unit): Closure<Any?> =
    KotlinClosure1(action, this, this)

/**
  * @param diktatExtension
 * @return returns sourceRootDir as projectDir for sarif report
 */
fun Project.getSourceRootDir(diktatExtension: DiktatExtension): Path? = when {
    diktatExtension.githubActions -> projectDir.toPath()
    diktatExtension.reporter == "sarif" -> projectDir.toPath()
    else -> null
}

/**
 * Create CLI flag to set reporter for ktlint based on [diktatExtension].
 * [DiktatExtension.githubActions] should have higher priority than a custom input.
 *
 * @param diktatExtension extension of type [DiktatExtension]
 * @return CLI flag as string
 */
fun Project.getReporterType(diktatExtension: DiktatExtension): String {
    val name = diktatExtension.reporter.trim()
    val validReporters = listOf("sarif", "plain", "json", "html")
    val reporterType = when {
        diktatExtension.githubActions -> {
            if (diktatExtension.reporter.isNotEmpty()) {
                logger.warn("`diktat.githubActions` is set to true, so custom reporter [$name] will be ignored and SARIF reporter will be used")
            }
            "sarif"
        }
        name.isEmpty() -> {
            logger.info("Reporter name was not set. Using 'plain' reporter")
            "plain"
        }
        name !in validReporters -> {
            logger.warn("Reporter name is invalid (provided value: [$name]). Falling back to 'plain' reporter")
            "plain"
        }
        else -> name
    }

    return reporterType
}

/**
 * Get destination file for Diktat report or null if stdout is used.
 * [DiktatExtension.githubActions] should have higher priority than a custom input.
 *
 * @param diktatExtension extension of type [DiktatExtension]
 * @return destination [File] or null if stdout is used
 */
internal fun Project.getOutputFile(diktatExtension: DiktatExtension): File? = when {
    diktatExtension.githubActions -> project.layout.buildDirectory
        .file("reports/diktat/diktat.sarif")
        .get()
        .asFile
        .also {
            Files.createDirectories(it.parentFile.toPath())
        }
    diktatExtension.output.isNotEmpty() -> file(diktatExtension.output)
    else -> null
}

/**
 * Whether SARIF reporter is enabled or not
 *
 * @param reporterFlag
 * @return whether SARIF reporter is enabled
 */
internal fun isSarifReporterActive(reporterFlag: String) = reporterFlag.contains("sarif")
