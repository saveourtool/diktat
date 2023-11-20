/**
 * All default reporters
 */

@file:Suppress("UnnecessaryAbstractClass")

package com.saveourtool.diktat.plugin.gradle.extension

import com.saveourtool.diktat.plugin.gradle.defaultReportLocation
import org.gradle.api.Project
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Provider
import java.io.File
import javax.inject.Inject

/**
 * A base interface for reporter
 *
 * @param extension extension of generated report
 * @param objectFactory
 * @param project
 * @property id identifier of reporter
 */
abstract class DefaultReporter @Inject constructor(
    val id: String,
    extension: String,
    objectFactory: ObjectFactory,
    project: Project,
) : Reporter {
    override val output: RegularFileProperty = objectFactory.fileProperty()
        .also { fileProperty ->
            fileProperty.convention(project.defaultReportLocation(extension = extension))
        }
}

/**
 * Plain reporter
 *
 * @param objectFactory
 * @param project
 */
abstract class PlainReporter @Inject constructor(
    objectFactory: ObjectFactory,
    project: Project,
) : DefaultReporter(
    id = "plain",
    extension = "txt",
    objectFactory,
    project,
) {
    /**
     * Remove the default value for plain to print to stdout by default
     */
    override val output: RegularFileProperty = objectFactory.fileProperty()
        .also { fileProperty ->
            fileProperty.set(null as File?)
        }
}

/**
 * JSON reporter
 *
 * @param objectFactory
 * @param project
 */
abstract class JsonReporter @Inject constructor(
    objectFactory: ObjectFactory,
    project: Project,
) : DefaultReporter(
    id = "json",
    extension = "json",
    objectFactory,
    project,
)

/**
 * SARIF reporter
 *
 * @param objectFactory
 * @param project
 */
abstract class SarifReporter @Inject constructor(
    objectFactory: ObjectFactory,
    project: Project,
) : DefaultReporter(
    id = "sarif",
    extension = "sarif",
    objectFactory,
    project,
)

/**
 * GitHub actions reporter
 *
 * @param objectFactory
 * @param project
 */
abstract class GitHubActionsReporter @Inject constructor(
    project: Project,
    objectFactory: ObjectFactory,
) : SarifReporter(objectFactory, project) {
    override val output: RegularFileProperty = objectFactory.fileProperty()
        .also { fileProperty ->
            fileProperty.convention(project.getGitHubActionReporterOutput())
                .finalizeValue()
        }

    /**
     * Location for merged output
     */
    val mergeOutput: RegularFileProperty = objectFactory.fileProperty()
        .also { fileProperty ->
            fileProperty.convention(project.getGitHubActionReporterMergeOutput())
                .finalizeValue()
        }

    companion object {
        /**
         * @return [RegularFile] for output
         */
        fun Project.getGitHubActionReporterOutput(): Provider<RegularFile> = defaultReportLocation(extension = "sarif")

        /**
         * @return [RegularFile] for mergeOutput
         */
        fun Project.getGitHubActionReporterMergeOutput(): Provider<RegularFile> =
            rootProject.defaultReportLocation(fileName = "diktat-merged", extension = "sarif")
    }
}

/**
 * Checkstyle reporter
 *
 * @param objectFactory
 * @param project
 */
abstract class CheckstyleReporter @Inject constructor(
    objectFactory: ObjectFactory,
    project: Project,
) : DefaultReporter(
    id = "checkstyle",
    extension = "xml",
    objectFactory,
    project,
)

/**
 * HTML reporter
 *
 * @param objectFactory
 * @param project
 */
abstract class HtmlReporter @Inject constructor(
    objectFactory: ObjectFactory,
    project: Project,
) : DefaultReporter(
    id = "html",
    extension = "html",
    objectFactory,
    project,
)
