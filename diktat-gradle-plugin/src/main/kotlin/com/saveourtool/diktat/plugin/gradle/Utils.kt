/**
 * Utilities for diktat gradle plugin
 */

@file:Suppress("FILE_NAME_MATCH_CLASS", "MatchingDeclarationName")

package com.saveourtool.diktat.plugin.gradle

import org.gradle.api.Project
import java.io.File
import java.nio.file.Path

/**
 * @param diktatExtension
 * @return returns sourceRootDir as projectDir for sarif report
 */
fun Project.getSourceRootDir(diktatExtension: DiktatExtension): Path? = when {
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
