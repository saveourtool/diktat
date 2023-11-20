package com.saveourtool.diktat.plugin.gradle.extension

import com.saveourtool.diktat.plugin.gradle.defaultReportLocation
import org.gradle.api.Project
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import javax.inject.Inject

/**
 * A base interface for reporter
 *
 * @property id identifier of reporter
 * @param extension extension of generated report
 * @param objectFactory
 * @param project
 */
abstract class DefaultReporter @Inject constructor(
    val id: String,
    extension: String,
    objectFactory: ObjectFactory,
    project: Project,
): Reporter() {
    override val output: RegularFileProperty = objectFactory.fileProperty()
        .also { fileProperty ->
            fileProperty.set(project.defaultReportLocation(extension = extension))
        }
}


abstract class PlainReporter @Inject constructor(
    objectFactory: ObjectFactory,
    project: Project,
): DefaultReporter("plain", "txt", objectFactory, project)

abstract class JsonReporter @Inject constructor(
    objectFactory: ObjectFactory,
    project: Project,
): DefaultReporter("json", "json", objectFactory, project)

abstract class SarifReporter @Inject constructor(
    objectFactory: ObjectFactory,
    project: Project,
): DefaultReporter("sarif", "sarif", objectFactory, project) {
    /**
     * Location for merged output
     */
    abstract val mergeOutput: RegularFileProperty
}

abstract class GithubActionsReporter @Inject constructor(
    project: Project,
    objectFactory: ObjectFactory,
) : SarifReporter(objectFactory, project) {
    override val mergeOutput: RegularFileProperty = objectFactory.fileProperty()
        .also { fileProperty ->
            fileProperty.set(project.rootProject.defaultReportLocation(fileName = "diktat-merged", extension = "sarif"))
        }
}

abstract class CheckstyleReporter @Inject constructor(
    objectFactory: ObjectFactory,
    project: Project,
): DefaultReporter("checkstyle", "xml", objectFactory, project)

abstract class HtmlReporter @Inject constructor(
    objectFactory: ObjectFactory,
    project: Project,
): DefaultReporter("html", "html", objectFactory, project)
