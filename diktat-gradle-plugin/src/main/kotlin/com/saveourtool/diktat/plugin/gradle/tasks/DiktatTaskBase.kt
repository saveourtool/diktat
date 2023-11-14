package com.saveourtool.diktat.plugin.gradle.tasks

import com.saveourtool.diktat.DiktatRunner
import com.saveourtool.diktat.DiktatRunnerArguments
import com.saveourtool.diktat.DiktatRunnerFactory
import com.saveourtool.diktat.api.DiktatProcessorListener
import com.saveourtool.diktat.ktlint.DiktatBaselineFactoryImpl
import com.saveourtool.diktat.ktlint.DiktatProcessorFactoryImpl
import com.saveourtool.diktat.ktlint.DiktatReporterFactoryImpl
import com.saveourtool.diktat.plugin.gradle.DiktatExtension
import com.saveourtool.diktat.plugin.gradle.getOutputFile
import com.saveourtool.diktat.plugin.gradle.getReporterType
import com.saveourtool.diktat.ruleset.rules.DiktatRuleConfigReaderImpl
import com.saveourtool.diktat.ruleset.rules.DiktatRuleSetFactoryImpl

import generated.DIKTAT_VERSION
import generated.KTLINT_VERSION
import org.gradle.api.GradleException
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.SourceTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.VerificationTask

import java.nio.file.Path

/**
 * A base task to run `diktat`
 *
 * @property extension
 */
abstract class DiktatTaskBase(
    @get:Internal internal val extension: DiktatExtension,
) : SourceTask(), VerificationTask {
    private val diktatRunnerFactory by lazy {
        DiktatRunnerFactory(
            diktatRuleConfigReader = DiktatRuleConfigReaderImpl(),
            diktatRuleSetFactory = DiktatRuleSetFactoryImpl(),
            diktatProcessorFactory = DiktatProcessorFactoryImpl(),
            diktatBaselineFactory = DiktatBaselineFactoryImpl(),
            diktatReporterFactory = DiktatReporterFactoryImpl()
        )
    }
    private val diktatRunnerArguments by lazy {
        DiktatRunnerArguments(
            configFile = extension.diktatConfigFile.toPath(),
            sourceRootDir = project.projectDir.toPath(),
            files = source.files.map { it.toPath() },
            baselineFile = extension.baseline?.let { project.file(it).toPath() },
            reporterType = project.getReporterType(extension),
            reporterOutput = project.getOutputFile(extension)?.outputStream(),
            loggingListener = object : DiktatProcessorListener {
                override fun beforeAll(files: Collection<Path>) {
                    project.logger.info("Analyzing {} files with diktat in project {}", files.size, project.name)
                    project.logger.debug("Analyzing {}", files)
                }
                override fun before(file: Path) {
                    project.logger.debug("Checking file {}", file)
                }
            }
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
        ignoreFailures = extension.ignoreFailures
        if (patternSet.includes.isEmpty() && patternSet.excludes.isEmpty()) {
            patternSet.include("src/**/*.kt")
        }
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
        if (source.isEmpty) {
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
}
