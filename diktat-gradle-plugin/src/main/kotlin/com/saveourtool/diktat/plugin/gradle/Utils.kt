/**
 * Utilities for diktat gradle plugin
 */

package com.saveourtool.diktat.plugin.gradle

import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.reporting.ReportingExtension

/**
 * @param fileName
 * @param extension
 * @return default location of report with provided [extension]
 */
internal fun Project.defaultReportLocation(
    extension: String,
    fileName: String = "diktat",
): Provider<RegularFile> = project.layout
    .buildDirectory
    .file("${ReportingExtension.DEFAULT_REPORTS_DIR_NAME}/diktat/$fileName.$extension")
