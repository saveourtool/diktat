package com.saveourtool.diktat.plugin.gradle.extension

import com.saveourtool.diktat.plugin.gradle.defaultReportLocation
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
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
) : Reporter() {
    override val output: RegularFileProperty = objectFactory.fileProperty()
        .also { fileProperty ->
            fileProperty.convention(project.defaultReportLocation(extension = extension))
        }
}

abstract class PlainReporter @Inject constructor(
    objectFactory: ObjectFactory,
    project: Project,
) : DefaultReporter(
    id = "plain",
    extension = "txt",
    objectFactory,
    project
)

abstract class JsonReporter @Inject constructor(
    objectFactory: ObjectFactory,
    project: Project,
) : DefaultReporter(
    id = "json",
    extension = "json",
    objectFactory,
    project
)

abstract class SarifReporter @Inject constructor(
    objectFactory: ObjectFactory,
    project: Project,
) : DefaultReporter(
    id = "sarif",
    extension = "sarif",
    objectFactory,
    project
) {
    /**
     * Location for merged output
     */
    abstract val mergeOutput: RegularFileProperty
}

abstract class GithubActionsReporter @Inject constructor(
    project: Project,
    objectFactory: ObjectFactory,
) : SarifReporter(objectFactory, project) {
    override val output: RegularFileProperty = objectFactory.fileProperty()
        .also { fileProperty ->
            fileProperty.convention(project.defaultReportLocation(extension = "sarif"))
                .finalizeValue()
        }

    override val mergeOutput: RegularFileProperty = objectFactory.fileProperty()
        .also { fileProperty ->
            fileProperty.convention(project.rootProject.defaultReportLocation(fileName = "diktat-merged", extension = "sarif"))
                .finalizeValue()
        }
}

abstract class CheckstyleReporter @Inject constructor(
    objectFactory: ObjectFactory,
    project: Project,
) : DefaultReporter(
    id = "checkstyle",
    extension = "xml",
    objectFactory,
    project
)

abstract class HtmlReporter @Inject constructor(
    objectFactory: ObjectFactory,
    project: Project,
) : DefaultReporter(
    id = "html",
    extension = "html",
    objectFactory,
    project
)
