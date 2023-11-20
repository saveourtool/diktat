package com.saveourtool.diktat.plugin.gradle.tasks

import com.saveourtool.diktat.plugin.gradle.DiktatExtension
import com.saveourtool.diktat.plugin.gradle.DiktatGradlePlugin.Companion.DIKTAT_CHECK_TASK
import com.saveourtool.diktat.plugin.gradle.DiktatGradlePlugin.Companion.MERGE_SARIF_REPORTS_TASK_NAME
import com.saveourtool.diktat.plugin.gradle.extension.GitHubActionsReporter
import com.saveourtool.diktat.plugin.gradle.extension.Reporters

import io.github.detekt.sarif4k.SarifSchema210
import org.gradle.api.DefaultTask
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.api.tasks.VerificationTask

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * A task to merge SARIF reports produced by diktat check / diktat fix tasks.
 */
abstract class SarifReportMergeTask : DefaultTask(), VerificationTask {
    /**
     * Source reports that should be merged
     */
    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val input: ConfigurableFileCollection

    /**
     * Destination for the merged report
     */
    @get:OutputFile
    @get:Optional
    abstract val output: RegularFileProperty

    /**
     * @throws TaskExecutionException if failed to deserialize SARIF
     */
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
                    "is SARIF reporter active?"
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

/**
 * @param diktatExtension extension of type [DiktatExtension]
 */
internal fun Project.configureMergeReportsTask(
    diktatExtension: DiktatExtension,
) {
    if (path == rootProject.path) {
        tasks.register(MERGE_SARIF_REPORTS_TASK_NAME, SarifReportMergeTask::class.java) { reportMergeTask ->
            diktatExtension.reporters
                .getGitHubActionsReporters()
                .firstOrNull()
                ?.let { gitHubActionsReporter ->
                    reportMergeTask.output.set(gitHubActionsReporter.mergeOutput)
                }
        }
    }

    val rootMergeSarifReportsTask = rootProject.tasks.named(MERGE_SARIF_REPORTS_TASK_NAME, SarifReportMergeTask::class.java)
    val diktatCheckTask = tasks.named(DIKTAT_CHECK_TASK, DiktatCheckTask::class.java)

    rootMergeSarifReportsTask.configure { reportMergeTask ->
        reportMergeTask.input.from(diktatCheckTask.map { task -> task.reporters.getGitHubActionsReporters().map { it.output } })
        reportMergeTask.dependsOn(diktatCheckTask)
    }
    diktatCheckTask.configure {
        it.finalizedBy(rootMergeSarifReportsTask)
    }
}

private fun Reporters.getGitHubActionsReporters() = all.filterIsInstance<GitHubActionsReporter>()
