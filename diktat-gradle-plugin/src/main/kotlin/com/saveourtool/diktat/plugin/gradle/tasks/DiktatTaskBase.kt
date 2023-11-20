package com.saveourtool.diktat.plugin.gradle.tasks

import com.saveourtool.diktat.DiktatRunner
import com.saveourtool.diktat.DiktatRunnerArguments
import com.saveourtool.diktat.DiktatRunnerFactory
import com.saveourtool.diktat.api.DiktatProcessorListener
import com.saveourtool.diktat.api.DiktatReporterCreationArguments
import com.saveourtool.diktat.ktlint.DiktatBaselineFactoryImpl
import com.saveourtool.diktat.ktlint.DiktatProcessorFactoryImpl
import com.saveourtool.diktat.ktlint.DiktatReporterFactoryImpl
import com.saveourtool.diktat.plugin.gradle.DiktatExtension
import com.saveourtool.diktat.plugin.gradle.extension.DefaultReporter
import com.saveourtool.diktat.plugin.gradle.extension.PlainReporter
import com.saveourtool.diktat.plugin.gradle.extension.Reporters
import com.saveourtool.diktat.ruleset.rules.DiktatRuleConfigReaderImpl
import com.saveourtool.diktat.ruleset.rules.DiktatRuleSetFactoryImpl

import generated.DIKTAT_VERSION
import generated.KTLINT_VERSION
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
 * @property extension
 * @param inputs
 */
@Suppress("WRONG_NEWLINES", "Deprecation")
abstract class DiktatTaskBase(
    @get:Internal internal val extension: DiktatExtension,
    private val inputs: PatternFilterable,
    objectFactory: ObjectFactory,
) : DefaultTask(), VerificationTask {
    @get:InputFile
    abstract val configFile: RegularFileProperty

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

    @get:Internal
    abstract val reporters: Reporters

    @get:Internal
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
    private val diktatReporterFactory by lazy {
        DiktatReporterFactoryImpl()
    }
    private val diktatRunnerFactory by lazy {
        DiktatRunnerFactory(
            diktatRuleConfigReader = DiktatRuleConfigReaderImpl(),
            diktatRuleSetFactory = DiktatRuleSetFactoryImpl(),
            diktatProcessorFactory = DiktatProcessorFactoryImpl(),
            diktatBaselineFactory = DiktatBaselineFactoryImpl(),
            diktatReporterFactory = diktatReporterFactory,
        )
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
                    id = reporter.id,
                    outputStream = reporter.output.map { file -> file.asFile.also { Files.createDirectories(it.parentFile.toPath()) }.outputStream() }.orNull,
                    sourceRootDir = sourceRootDir.takeIf { reporter.id == "sarif" },
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
            configInputStream = configFile.get().asFile.inputStream(),
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
        fun TaskProvider<out DiktatTaskBase>.configure(extension: DiktatExtension) {
            configure { task ->
                task.configFile.set(task.project.file(extension.diktatConfigFile))
                extension.baseline?.let { baseline -> task.baselineFile.set(task.project.file(baseline)) }
                task.ignoreFailures = extension.ignoreFailures
                task.reporters.all.addAll(extension.reporters.all)
            }
        }
    }
}
