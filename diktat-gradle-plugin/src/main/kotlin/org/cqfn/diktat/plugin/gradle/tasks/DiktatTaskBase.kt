package org.cqfn.diktat.plugin.gradle.tasks

import org.cqfn.diktat.DiktatProcessor
import org.cqfn.diktat.api.DiktatBaseline
import org.cqfn.diktat.api.DiktatBaseline.Companion.skipKnownErrors
import org.cqfn.diktat.api.DiktatBaselineFactory
import org.cqfn.diktat.api.DiktatProcessorListener
import org.cqfn.diktat.api.DiktatProcessorListener.Companion.closeAfterAllAsProcessorListener
import org.cqfn.diktat.api.DiktatProcessorListener.Companion.countErrorsAsProcessorListener
import org.cqfn.diktat.api.DiktatReporter
import org.cqfn.diktat.api.DiktatReporterFactory
import org.cqfn.diktat.ktlint.DiktatBaselineFactoryImpl
import org.cqfn.diktat.ktlint.DiktatProcessorFactoryImpl
import org.cqfn.diktat.ktlint.DiktatReporterFactoryImpl
import org.cqfn.diktat.plugin.gradle.DiktatExtension
import org.cqfn.diktat.plugin.gradle.DiktatJavaExecTaskBase
import org.cqfn.diktat.plugin.gradle.getOutputFile
import org.cqfn.diktat.plugin.gradle.getReporterType

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

import java.nio.file.Path
import java.util.concurrent.atomic.AtomicInteger

/**
 * A base task to run `diktat`
 * @property extension
 */
@Suppress("WRONG_NEWLINES")
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
        DiktatProcessorFactoryImpl().create(extension.diktatConfigFile.toPath())
    }

    /**
     * A baseline factory
     */
    @get:Internal
    internal val baselineFactory: DiktatBaselineFactory by lazy {
        DiktatBaselineFactoryImpl()
    }

    /**
     * A baseline loaded from provided file or empty
     */
    @get:Internal
    internal val baseline: DiktatBaseline? by lazy {
        extension.baseline?.let {
            baselineFactory.tryToLoad(
                baselineFile = project.file(it).toPath(),
                sourceRootDir = project.projectDir.toPath()
            )
        }
    }

    /**
     * A reporter factory
     */
    @get:Internal
    internal val reporterFactory: DiktatReporterFactory by lazy {
        DiktatReporterFactoryImpl()
    }

    /**
     * A reporter created based on configuration
     */
    @get:Internal
    internal val processorListener: DiktatProcessorListener by lazy {
        createReporter()
    }

    init {
        ignoreFailures = extension.ignoreFailures
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
            val loggingListener = object : DiktatProcessorListener.Companion.Empty() {
                override fun before(file: Path) {
                    project.logger.debug("Checking file $file")
                }
            }
            val (baseline, baselineGeneratorListener) = extension.baseline
                ?.let {
                    baselineFactory.tryToLoad(
                        baselineFile = project.file(it).toPath(),
                        sourceRootDir = project.projectDir.toPath()
                    )
                }
                ?.let { it to DiktatProcessorListener.empty }
                ?: run {
                    val baselineGenerator = extension.baseline
                        ?.let {
                            baselineFactory.generator(
                                project.file(it).toPath(),
                                project.rootDir.toPath()
                            )
                        }
                        ?: DiktatProcessorListener.empty
                    DiktatBaseline.empty to baselineGenerator
                }
            val errorCounter = AtomicInteger()
            val files = actualInputs.files
                .also { files ->
                    project.logger.info("Analyzing ${files.size} files with diktat in project ${project.name}")
                    project.logger.debug("Analyzing $files")
                }
                .asSequence()
                .map { it.toPath() }
            doRun(
                diktatProcessor = diktatProcessor,
                listener = DiktatProcessorListener(
                    processorListener.skipKnownErrors(baseline),
                    baselineGeneratorListener,
                    errorCounter.countErrorsAsProcessorListener(),
                    loggingListener
                ),
                files = files
            ) { file, formattedText ->
                val fileName = file.toFile().absolutePath
                val fileContent = file.toFile().readText(Charsets.UTF_8)
                if (fileContent != formattedText) {
                    project.logger.info("Original and formatted content differ, writing to $fileName...")
                    file.toFile().writeText(formattedText, Charsets.UTF_8)
                }
            }
            if (errorCounter.get() > 0 && !ignoreFailures) {
                throw GradleException("There are ${errorCounter.get()} lint errors")
            }
        }
    }

    /**
     * An abstract method which should be overridden by fix and check tasks
     *
     * @param diktatProcessor
     * @param listener
     * @param files
     * @param formattedContentConsumer
     */
    protected abstract fun doRun(
        diktatProcessor: DiktatProcessor,
        listener: DiktatProcessorListener,
        files: Sequence<Path>,
        formattedContentConsumer: (Path, String) -> Unit
    )

    private fun createReporter(): DiktatReporter {
        val reporterType = project.getReporterType(extension)
        val (outputStream, closeListener) = project.getOutputFile(extension)?.outputStream()?.let {
            it to it.closeAfterAllAsProcessorListener()
        } ?: (System.out to DiktatProcessorListener.empty)
        val actualReporter = reporterFactory(reporterType, outputStream ?: System.`out`, emptyMap(), project.rootDir.toPath())
        return  DiktatProcessorListener(actualReporter, closeListener)
    }
}
