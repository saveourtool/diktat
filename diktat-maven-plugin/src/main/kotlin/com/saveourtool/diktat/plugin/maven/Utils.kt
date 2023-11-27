/**
 * Utilities for diktat maven plugin
 */

package com.saveourtool.diktat.plugin.maven

import com.saveourtool.diktat.api.DiktatReporterType
import org.apache.maven.project.MavenProject
import java.io.File

/**
 * @param reporterType
 * @return default location of report with provided [reporterType]
 */
internal fun MavenProject.defaultReportLocation(
    reporterType: DiktatReporterType,
): File = basedir
    .resolve(build.directory)
    .resolve("reports")
    .resolve("diktat")
    .resolve("diktat.${reporterType.extension}")
