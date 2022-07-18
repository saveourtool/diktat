package org.cqfn.diktat.plugin.gradle.tasks

import org.cqfn.diktat.plugin.gradle.DiktatExtension
import org.cqfn.diktat.plugin.gradle.DiktatJavaExecTaskBase
import org.cqfn.diktat.plugin.gradle.createReporterFlag
import org.cqfn.diktat.plugin.gradle.getOutputFile
import org.cqfn.diktat.plugin.gradle.isSarifReporterActive
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
        val sarifReports = inputs.files
            .filter { it.exists() }
            .map {
                try {
                    Json.decodeFromString<SarifSchema210>(it.readText())
                } catch (e: SerializationException) {
                    logger.error("Couldn't deserialize JSON: is ${it.canonicalPath} a SARIF file?")
                    throw TaskExecutionException(this, e)
                }
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

internal fun Project.registerMergeReportsTask(diktatExtension: DiktatExtension) {
    val reportMergeTaskTaskProvider = tasks.register("mergeDiktatReports", SarifReportMergeTask::class.java) { reportMergeTask ->
        reportMergeTask.enabled = isSarifReporterActive(
            createReporterFlag(diktatExtension)
        )
        allprojects.forEach { subproject ->
            val diktatOutputFile =
                subproject.extensions.findByType(DiktatExtension::class.java)?.let {
                    subproject.getOutputFile(it)
                }
            diktatOutputFile?.let { reportMergeTask.input.from(it) }
        }
        val diktatReportsDir = "${project.buildDir}/reports/diktat"
        reportMergeTask.outputs.dir(diktatReportsDir)
        reportMergeTask.output.set(
            project.file("$diktatReportsDir/diktat-merged.sarif")
        )
    }
    allprojects.forEach { subproject ->
        reportMergeTaskTaskProvider.configure {
//            it.dependsOn(subproject.tasks.withType(DiktatJavaExecTaskBase::class.java))
            it.shouldRunAfter(
                subproject.tasks.withType(DiktatJavaExecTaskBase::class.java)
            )
        }
    }
}
