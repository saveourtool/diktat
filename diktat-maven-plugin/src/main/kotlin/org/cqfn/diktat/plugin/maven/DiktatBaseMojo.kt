package org.cqfn.diktat.plugin.maven

import org.cqfn.diktat.DiktatRunner
import org.cqfn.diktat.DiktatRunnerArguments
import org.cqfn.diktat.DiktatRunnerFactory
import org.cqfn.diktat.ktlint.DiktatBaselineFactoryImpl
import org.cqfn.diktat.ktlint.DiktatProcessorFactoryImpl
import org.cqfn.diktat.ktlint.DiktatReporterFactoryImpl
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetFactoryImpl

import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.Mojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.io.path.Path

/**
 * Base [Mojo] for checking and fixing code using diktat
 */
abstract class DiktatBaseMojo : AbstractMojo() {
    /**
     * Flag that indicates whether to turn debug logging on
     */
    @Parameter(property = "diktat.debug")
    var debug = false

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

    @Parameter(defaultValue = "\${session}", readonly = true)
    private lateinit var mavenSession: MavenSession

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
        if (!File(configFile).exists()) {
            throw MojoExecutionException("Configuration file $diktatConfigFile doesn't exist")
        }
        log.info("Running diKTat plugin with configuration file $configFile and inputs $inputs" +
                if (excludes.isNotEmpty()) " and excluding $excludes" else ""
        )

        val sourceRootDir = mavenProject.basedir.parentFile.toPath()
        val diktatRunnerFactory = DiktatRunnerFactory(
            diktatRuleSetFactory = DiktatRuleSetFactoryImpl(),
            diktatProcessorFactory = DiktatProcessorFactoryImpl(),
            diktatBaselineFactory = DiktatBaselineFactoryImpl(),
            diktatReporterFactory = DiktatReporterFactoryImpl()
        )
        val args = DiktatRunnerArguments(
            configFileName = resolveConfig(),
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
     * @return path to configuration file as a string. File by this path might not exist.
     */
    private fun resolveConfig(): String {
        if (File(diktatConfigFile).isAbsolute) {
            return diktatConfigFile
        }

        return generateSequence(mavenProject) { it.parent }
            .map { File(it.basedir, diktatConfigFile) }
            .run {
                firstOrNull { it.exists() } ?: first()
            }
            .absolutePath
    }
}
