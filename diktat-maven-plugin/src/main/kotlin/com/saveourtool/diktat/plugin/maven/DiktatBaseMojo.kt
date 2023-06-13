package com.saveourtool.diktat.plugin.maven

import com.saveourtool.diktat.DiktatRunner
import com.saveourtool.diktat.DiktatRunnerArguments
import com.saveourtool.diktat.DiktatRunnerFactory
import com.saveourtool.diktat.ktlint.DiktatBaselineFactoryImpl
import com.saveourtool.diktat.ktlint.DiktatProcessorFactoryImpl
import com.saveourtool.diktat.ktlint.DiktatReporterFactoryImpl
import com.saveourtool.diktat.ruleset.rules.DiktatRuleConfigReaderImpl
import com.saveourtool.diktat.ruleset.rules.DiktatRuleSetFactoryImpl

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.Mojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.Path
import kotlin.io.path.inputStream
import kotlin.io.path.isRegularFile

/**
 * Base [Mojo] for checking and fixing code using diktat
 */
abstract class DiktatBaseMojo : AbstractMojo() {
    /**
     * Property that will be used if you need to publish the report to GitHub
     */
    @Parameter(property = "diktat.githubActions")
    var githubActions = false

    /**
     * Type of the reporter to use
     */
    @Parameter(property = "diktat.reporter")
    var reporter = "plain"

    /**
     * Type of output
     * Default: System.out
     */
    @Parameter(property = "diktat.output")
    var output = ""

    /**
     * Baseline file, containing a list of errors that will be ignored.
     * If this file doesn't exist, it will be created on the first invocation.
     * Default: no baseline.
     */
    @Parameter(property = "diktat.baseline")
    var baseline: File? = null

    /**
     * Path to diktat yml config file. Can be either absolute or relative to project's root directory.
     */
    @Parameter(property = "diktat.config", defaultValue = "diktat-analysis.yml")
    lateinit var diktatConfigFile: String

    /**
     * Property that can be used to access various maven settings
     */
    @Parameter(defaultValue = "\${project}", readonly = true)
    private lateinit var mavenProject: MavenProject

    /**
     * Paths that will be scanned for .kt(s) files
     */
    @Parameter(property = "diktat.inputs", defaultValue = "\${project.basedir}/src")
    lateinit var inputs: List<String>

    /**
     * Paths that will be excluded if encountered during diktat run
     */
    @Parameter(property = "diktat.excludes", defaultValue = "")
    lateinit var excludes: List<String>

    /**
     * @param runner instance of [DiktatRunner] used in analysis
     * @param args arguments for [DiktatRunner]
     * @return count of errors
     */
    @Suppress("TOO_MANY_PARAMETERS")
    abstract fun runAction(
        runner: DiktatRunner,
        args: DiktatRunnerArguments,
    ): Int

    /**
     * Perform code check using diktat ruleset
     *
     * @throws MojoFailureException if code style check was not passed
     * @throws MojoExecutionException if an exception in __KtLint__ has been thrown
     */
    override fun execute() {
        val configFile = resolveConfig()
        if (configFile.isRegularFile()) {
            throw MojoExecutionException("Configuration file $diktatConfigFile doesn't exist")
        }
        log.info("Running diKTat plugin with configuration file $configFile and inputs $inputs" +
                if (excludes.isNotEmpty()) " and excluding $excludes" else ""
        )

        val sourceRootDir = mavenProject.basedir.parentFile.toPath()
        val diktatRunnerFactory = DiktatRunnerFactory(
            diktatRuleConfigReader = DiktatRuleConfigReaderImpl(),
            diktatRuleSetFactory = DiktatRuleSetFactoryImpl(),
            diktatProcessorFactory = DiktatProcessorFactoryImpl(),
            diktatBaselineFactory = DiktatBaselineFactoryImpl(),
            diktatReporterFactory = DiktatReporterFactoryImpl()
        )
        val args = DiktatRunnerArguments(
            configInputStream = configFile.inputStream(),
            sourceRootDir = sourceRootDir,
            files = inputs.map(::Path),
            baselineFile = baseline?.toPath(),
            reporterType = getReporterType(),
            reporterOutput = getReporterOutput(),
        )
        val diktatRunner = diktatRunnerFactory(args)
        val errorCounter = runAction(
            runner = diktatRunner,
            args = args,
        )
        if (errorCounter > 0) {
            throw MojoFailureException("There are $errorCounter lint errors")
        }
    }

    private fun getReporterType(): String = if (githubActions) {
        "sarif"
    } else if (reporter in setOf("sarif", "plain", "json", "html")) {
        reporter
    } else {
        log.warn("Reporter name ${this.reporter} was not specified or is invalid. Falling to 'plain' reporter")
        "plain"
    }

    private fun getReporterOutput(): OutputStream? = if (output.isNotBlank()) {
        FileOutputStream(this.output, false)
    } else if (githubActions) {
        FileOutputStream("${mavenProject.basedir}/${mavenProject.name}.sarif", false)
    } else {
        null
    }

    /**
     * Function that searches diktat config file in maven project hierarchy.
     * If [diktatConfigFile] is absolute, it's path is used. If [diktatConfigFile] is relative, this method looks for it in all maven parent projects.
     * This way config file can be placed in parent module directory and used in all child modules too.
     *
     * @return a configuration file. File by this path might not exist.
     */
    private fun resolveConfig(): Path {
        val file = Paths.get(diktatConfigFile)
        if (file.isAbsolute) {
            return file
        }

        return generateSequence(mavenProject) { it.parent }
            .map { it.basedir.toPath().resolve(diktatConfigFile) }
            .run {
                firstOrNull { it.isRegularFile() } ?: first()
            }
    }
}
