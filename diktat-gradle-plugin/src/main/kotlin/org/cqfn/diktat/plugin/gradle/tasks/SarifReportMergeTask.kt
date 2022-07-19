package org.cqfn.diktat.plugin.gradle.tasks

import org.cqfn.diktat.plugin.gradle.DiktatExtension
import org.cqfn.diktat.plugin.gradle.DiktatJavaExecTaskBase
import org.cqfn.diktat.plugin.gradle.getOutputFile
import io.github.detekt.sarif4k.SarifSchema210
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

abstract class SarifReportMergeTask : DefaultTask() {
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val input: ConfigurableFileCollection

    @get:OutputFile
    abstract val output: RegularFileProperty

    @TaskAction
    fun mergeReports() {
        val sarifReports = input.files
            .filter { it.exists() }
            .also { logger.info("Merging SARIF reports from files $it") }
            .map {
                try {
                    Json.decodeFromString<SarifSchema210>(it.readText())
                } catch (e: SerializationException) {
                    logger.error("Couldn't deserialize JSON: is ${it.canonicalPath} a SARIF file?")
                    throw TaskExecutionException(this, e)
                }
            }

        if (sarifReports.isEmpty()) {
            logger.warn("Cannot perform merging of SARIF reports because no matching files were found; " +
                    "Is SARIF reporter active?"
            )
            return
        }

        // All reports should contain identical metadata, so we are using the first one as a base.
        val templateReport = sarifReports.first()
        val allResults = sarifReports.flatMap { sarifSchema ->
            sarifSchema.runs
                .flatMap { it.results.orEmpty() }
        }
        val mergedSarif = templateReport.copy(
            runs = listOf(templateReport.runs.first().copy(results = allResults))
        )

        output.get().asFile.writeText(Json.encodeToString(mergedSarif))
    }
}

internal fun Project.configureMergeReportsTask(diktatExtension: DiktatExtension) {
    if (path == rootProject.path) {
        tasks.register("mergeDiktatReports", SarifReportMergeTask::class.java) { reportMergeTask ->
            val diktatReportsDir = "${project.buildDir}/reports/diktat"
            val mergedReportFile = project.file("$diktatReportsDir/diktat-merged.sarif")
            reportMergeTask.outputs.file(mergedReportFile)
            reportMergeTask.output.set(mergedReportFile)
        }
    }
//    val diktatOutputFile = objects.fileProperty().convention(
//        { getOutputFile(diktatExtension) }
//    )
    val reportMergeTaskTaskProvider = rootProject.tasks.named("mergeDiktatReports", SarifReportMergeTask::class.java) { reportMergeTask ->
        getOutputFile(diktatExtension)?.let { reportMergeTask.input.from(it) }
        reportMergeTask.shouldRunAfter(tasks.withType(DiktatJavaExecTaskBase::class.java))
    }
    tasks.withType(DiktatJavaExecTaskBase::class.java).configureEach { diktatJavaExecTaskBase ->
        diktatJavaExecTaskBase.finalizedBy(reportMergeTaskTaskProvider)
    }
}
