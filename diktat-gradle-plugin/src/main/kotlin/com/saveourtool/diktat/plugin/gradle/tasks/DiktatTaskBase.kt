package com.saveourtool.diktat.plugin.gradle.tasks

import com.saveourtool.diktat.DiktatRunner
import com.saveourtool.diktat.DiktatRunnerArguments
import com.saveourtool.diktat.DiktatRunnerFactory
import com.saveourtool.diktat.ENGINE_INFO
import com.saveourtool.diktat.api.DiktatProcessorListener
import com.saveourtool.diktat.api.DiktatReporterCreationArguments
import com.saveourtool.diktat.api.DiktatReporterType
import com.saveourtool.diktat.diktatRunnerFactory
import com.saveourtool.diktat.plugin.gradle.DiktatExtension
import com.saveourtool.diktat.plugin.gradle.extension.DefaultReporter
import com.saveourtool.diktat.plugin.gradle.extension.PlainReporter
import com.saveourtool.diktat.plugin.gradle.extension.Reporters

import generated.DIKTAT_VERSION
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.model.ObjectFactory
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.VerificationTask
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.language.base.plugins.LifecycleBasePlugin

import java.nio.file.Files
import java.nio.file.Path

/**
 * A base task to run `diktat`
 *
 * @param inputs
 * @property extension
 */
@Suppress("WRONG_NEWLINES", "Deprecation")
abstract class DiktatTaskBase(
    @get:Internal internal val extension: DiktatExtension,
    private val inputs: PatternFilterable,
    objectFactory: ObjectFactory,
) : DefaultTask(), VerificationTask {
    /**
     * Config file
     */
    @get:Optional
    @get:InputFile
    abstract val configFile: RegularFileProperty

    /**
     * Baseline
     */
    @get:Optional
    @get:InputFile
    abstract val baselineFile: RegularFileProperty

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
     * All reporters
     */
    @get:Internal
    val reporters: Reporters = objectFactory.newInstance(Reporters::class.java)

    /**
     * Outputs for all reporters
     */
    @get:OutputFiles
    @get:Optional
    val reporterOutputs: ConfigurableFileCollection = objectFactory.fileCollection()
        .also { fileCollection ->
            fileCollection.setFrom(reporters.all.mapNotNull { it.output.orNull })
            fileCollection.finalizeValue()
        }

    /**
     * Whether diktat should be executed
     */
    @get:Internal
    internal val shouldRun: Boolean by lazy {
        !actualInputs.isEmpty
    }
    private val diktatRunnerArguments by lazy {
        val sourceRootDir by lazy {
            project.rootProject.projectDir.toPath()
        }
        val defaultPlainReporter by lazy {
            project.objects.newInstance(PlainReporter::class.java)
        }
        val reporterCreationArgumentsList = (reporters.all.takeUnless { it.isEmpty() } ?: listOf(defaultPlainReporter))
            .filterIsInstance<DefaultReporter>()
            .map { reporter ->
                DiktatReporterCreationArguments(
                    reporterType = reporter.type,
                    outputStream = reporter.output.map { file -> file.asFile.also { Files.createDirectories(it.parentFile.toPath()) }.outputStream() }.orNull,
                    sourceRootDir = sourceRootDir.takeIf { reporter.type == DiktatReporterType.SARIF },
                )
            }
        val loggingListener = object : DiktatProcessorListener {
            override fun beforeAll(files: Collection<Path>) {
                project.logger.info("Analyzing {} files with diktat in project {}", files.size, project.name)
                project.logger.debug("Analyzing {}", files)
            }
            override fun before(file: Path) {
                project.logger.debug("Checking file {}", file)
            }
        }
        DiktatRunnerArguments(
            configInputStream = configFile.map { it.asFile.inputStream() }.orNull,
            sourceRootDir = sourceRootDir,
            files = actualInputs.files.map { it.toPath() },
            baselineFile = baselineFile.map { it.asFile.toPath() }.orNull,
            reporterArgsList = reporterCreationArgumentsList,
            loggingListener = loggingListener,
        )
    }

    /**
     * [DiktatRunner] created based on a default [DiktatRunnerFactory]
     */
    @get:Internal
    val diktatRunner by lazy {
        diktatRunnerFactory(diktatRunnerArguments)
    }

    init {
        group = LifecycleBasePlugin.VERIFICATION_GROUP
    }

    /**
     * Function to execute diKTat
     *
     * @throws GradleException
     */
    @TaskAction
    fun run() {
        if (extension.debug) {
            project.logger.lifecycle("Running diktat $DIKTAT_VERSION with $ENGINE_INFO")
        }
        if (!shouldRun) {
            /*
             If ktlint receives empty patterns, it implicitly uses &#42;&#42;/*.kt, **/*.kts instead.
             This can lead to diktat analyzing gradle buildscripts and so on. We want to prevent it.
             */
            project.logger.warn("Inputs for $name do not exist, will not run diktat")
            project.logger.info("Skipping diktat execution")
        } else {
            doRun()
        }
    }

    private fun doRun() {
        val errorCounter = doRun(
            runner = diktatRunner,
            args = diktatRunnerArguments
        )
        if (errorCounter > 0 && !ignoreFailures) {
            throw GradleException("There are $errorCounter lint errors")
        }
    }

    /**
     * An abstract method which should be overridden by fix and check tasks
     *
     * @param runner instance of [DiktatRunner] used in analysis
     * @param args arguments for [DiktatRunner]
     * @return count of errors
     */
    abstract fun doRun(
        runner: DiktatRunner,
        args: DiktatRunnerArguments
    ): Int

    companion object {
        /**
         * @param extension
         */
        fun TaskProvider<out DiktatTaskBase>.configure(extension: DiktatExtension) {
            configure { task ->
                extension.diktatConfigFile?.let { diktatConfigFile -> task.configFile.set(task.project.file(diktatConfigFile)) }
                extension.baseline?.let { baseline -> task.baselineFile.set(task.project.file(baseline)) }
                task.ignoreFailures = extension.ignoreFailures
                task.reporters.all.addAll(extension.reporters.all)
                if (extension.githubActions) {
                    task.reporters.gitHubActions()
                }
            }
        }
    }
}
