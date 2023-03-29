@file:Suppress(
    "Deprecation"
)

package org.cqfn.diktat.plugin.gradle.tasks

import org.cqfn.diktat.DiktatProcessCommand
import org.cqfn.diktat.DiktatProcessor
import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.api.DiktatLogLevel
import org.cqfn.diktat.ktlint.LintErrorReporter
import org.cqfn.diktat.ktlint.unwrap
import org.cqfn.diktat.plugin.gradle.DiktatExtension
import org.cqfn.diktat.plugin.gradle.getOutputFile
import org.cqfn.diktat.plugin.gradle.getReporterType
import org.cqfn.diktat.plugin.gradle.isSarifReporterActive

import com.pinterest.ktlint.core.Reporter
import com.pinterest.ktlint.core.internal.CurrentBaseline
import com.pinterest.ktlint.core.internal.containsLintError
import com.pinterest.ktlint.core.internal.loadBaseline
import com.pinterest.ktlint.reporter.baseline.BaselineReporter
import com.pinterest.ktlint.reporter.html.HtmlReporter
import com.pinterest.ktlint.reporter.json.JsonReporter
import com.pinterest.ktlint.reporter.plain.PlainReporter
import com.pinterest.ktlint.reporter.sarif.SarifReporter
import generated.DIKTAT_VERSION
import generated.KTLINT_VERSION
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask
import org.gradle.api.tasks.util.PatternFilterable

import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream

/**
 * A base task to run `diktat`
 * @property extension
 */
abstract class DiktatTaskBase(
    @get:Internal internal val extension: DiktatExtension,
    private val inputs: PatternFilterable
) : DefaultTask(), VerificationTask, DiktatJavaExecTaskBase {
    /**
     * Files that will be analyzed by diktat
     */
    @get:IgnoreEmptyDirectories
    @get:SkipWhenEmpty
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val actualInputs: FileCollection by lazy {
        if (inputs.includes.isEmpty() && inputs.excludes.isEmpty()) {
            inputs.include("src/**/*.kt")
        }
        project.objects.fileCollection().from(
            project.fileTree("${project.projectDir}").apply {
                exclude("${project.buildDir}")
            }
                .matching(inputs)
        )
    }

    /**
     * Whether diktat should be executed
     */
    @get:Internal
    internal val shouldRun: Boolean by lazy {
        !actualInputs.isEmpty
    }

    /**
     * [DiktatProcessor] created from [extension]
     */
    @get:Internal
    internal val diktatProcessor: DiktatProcessor by lazy {
        DiktatProcessor.builder()
            .diktatRuleSetProvider(extension.diktatConfigFile.toPath())
            .logLevel(
                if (extension.debug) {
                    DiktatLogLevel.DEBUG
                } else {
                    DiktatLogLevel.INFO
                }
            )
            .build()
    }

    /**
     * A baseline loaded from provided file or empty
     */
    @get:Internal
    internal val baseline: CurrentBaseline by lazy {
        extension.baseline?.let { loadBaseline(it) }
            ?: CurrentBaseline(emptyMap(), false)
    }

    /**
     * A reporter created based on configuration
     */
    @get:Internal
    internal val reporter: Reporter by lazy {
        resolveReporter(baseline)
    }

    /**
     * Function to execute diKTat
     *
     * @throws GradleException
     */
    @TaskAction
    fun run() {
        if (extension.debug) {
            project.logger.lifecycle("Running diktat $DIKTAT_VERSION with ktlint $KTLINT_VERSION")
        }
        if (!shouldRun) {
            /*
             If ktlint receives empty patterns, it implicitly uses &#42;&#42;/*.kt, **/*.kts instead.
             This can lead to diktat analyzing gradle buildscripts and so on. We want to prevent it.
             */
            project.logger.warn("Inputs for $name do not exist, will not run diktat")
            project.logger.info("Skipping diktat execution")
        } else {
            reporter.beforeAll()
            val lintErrorReporter = LintErrorReporter()
            actualInputs.files
                .also { files ->
                    project.logger.info("Analyzing ${files.size} files with diktat in project ${project.name}")
                    project.logger.debug("Analyzing $files")
                }
                .forEach { file ->
                    processFile(
                        file = file,
                        diktatProcessor = diktatProcessor,
                        reporter = Reporter.from(reporter, lintErrorReporter)
                    )
                }
            reporter.afterAll()
            if (lintErrorReporter.isNotEmpty() && !ignoreFailures) {
                throw GradleException("There are ${lintErrorReporter.errorCount()} lint errors")
            }
        }
    }

    private fun processFile(
        file: File,
        diktatProcessor: DiktatProcessor,
        reporter: Reporter
    ) {
        project.logger.lifecycle("Checking file $file")
        reporter.before(file.absolutePath)
        val baselineErrors = baseline.baselineRules?.get(
            file.relativeTo(project.projectDir).invariantSeparatorsPath
        ) ?: emptyList()
        val diktatCallback = DiktatCallback { error, isCorrected ->
            val ktLintError = error.unwrap()
            if (!baselineErrors.containsLintError(ktLintError)) {
                reporter.onLintError(file.absolutePath, ktLintError, isCorrected)
            }
        }
        val command = DiktatProcessCommand.builder()
            .processor(diktatProcessor)
            .file(file.toPath())
            .callback(diktatCallback)
            .build()
        doRun(command) { formattedText ->
            val fileName = file.absolutePath
            val fileContent = file.readText(Charsets.UTF_8)
            if (fileContent != formattedText) {
                project.logger.info("Original and formatted content differ, writing to $fileName...")
                file.writeText(formattedText, Charsets.UTF_8)
            }
        }
        reporter.after(file.absolutePath)
    }

    /**
     * An abstract method which should be overridden by fix and check tasks
     *
     * @param diktatCommand
     * @param formattedContentConsumer
     */
    protected abstract fun doRun(diktatCommand: DiktatProcessCommand, formattedContentConsumer: (String) -> Unit)

    private fun resolveReporter(baselineResults: CurrentBaseline): Reporter {
        val reporterType = project.getReporterType(extension)
        if (isSarifReporterActive(reporterType)) {
            // need to set user.home specially for ktlint, so it will be able to put a relative path URI in SARIF
            System.setProperty("user.home", project.rootDir.toString())
        }
        val output = project.getOutputFile(extension)?.outputStream()?.let { PrintStream(it) } ?: System.`out`
        val actualReporter = if (extension.githubActions) {
            SarifReporter(output)
        } else {
            when (reporterType) {
                "sarif" -> SarifReporter(output)
                "plain" -> PlainReporter(output)
                "json" -> JsonReporter(output)
                "html" -> HtmlReporter(output)
                else -> {
                    project.logger.warn("Reporter name $reporterType was not specified or is invalid. Falling to 'plain' reporter")
                    PlainReporter(output)
                }
            }
        }

        return if (baselineResults.baselineGenerationNeeded) {
            val baseline = requireNotNull(extension.baseline) {
                "baseline is not provided, but baselineGenerationNeeded is true"
            }
            val baselineReporter = BaselineReporter(PrintStream(FileOutputStream(baseline, true)))
            return Reporter.from(actualReporter, baselineReporter)
        } else {
            actualReporter
        }
    }
}
