package com.saveourtool.diktat.plugin.maven

import com.saveourtool.diktat.DiktatRunner
import com.saveourtool.diktat.DiktatRunnerArguments
import com.saveourtool.diktat.diktatRunnerFactory
import com.saveourtool.diktat.plugin.maven.reporters.GitHubActionsReporter
import com.saveourtool.diktat.plugin.maven.reporters.PlainReporter
import com.saveourtool.diktat.plugin.maven.reporters.Reporter
import com.saveourtool.diktat.plugin.maven.reporters.Reporters
import com.saveourtool.diktat.util.isKotlinCodeOrScript

import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.Mojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
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
     * The reporters to use
     */
    @Parameter
    var reporters: Reporters? = null

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
        log.info("Running diKTat plugin with ${configFile?.let { "configuration file $it" } ?: "default configuration" } and inputs $inputs" +
                if (excludes.isNotEmpty()) " and excluding $excludes" else ""
        )

        val sourceRootDir = getSourceRootDirTransitive()
        val reporters: List<Reporter> = (reporters?.getAll() ?: listOf(PlainReporter()))
            .let { all ->
                if (githubActions && all.filterIsInstance<GitHubActionsReporter>().isEmpty()) {
                    all + GitHubActionsReporter()
                } else {
                    all
                }
            }

        val reporterArgsList = reporters.map { it.toCreationArguments(mavenProject, sourceRootDir) }
        val args = DiktatRunnerArguments(
            configInputStream = configFile?.inputStream(),
            sourceRootDir = sourceRootDir,
            files = files(),
            baselineFile = baseline?.toPath(),
            reporterArgsList = reporterArgsList,
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

    /**
     * Function that searches diktat config file in maven project hierarchy.
     * If [diktatConfigFile] is absolute, it's path is used. If [diktatConfigFile] is relative, this method looks for it in all maven parent projects.
     * This way config file can be placed in parent module directory and used in all child modules too.
     *
     * @return a configuration file. File by this path exists.
     */
    private fun resolveConfig(): Path? {
        val file = Paths.get(diktatConfigFile)
        if (file.isAbsolute) {
            return file
        }

        return generateSequence(mavenProject) { it.parent }
            .map { it.basedir.toPath().resolve(diktatConfigFile) }
            .firstOrNull { it.isRegularFile() }
    }

    private fun getSourceRootDirTransitive(): Path = generateSequence(mavenProject) { project ->
        val parent = project.parent
        parent?.basedir?.let {
            parent
        }
    }.last().basedir.toPath()

    private fun files(): List<Path> {
        val (excludedDirs, excludedFiles) = excludes.map(::File).partition { it.isDirectory }
        return inputs
            .asSequence()
            .map(::File)
            .flatMap {
                it.files(excludedDirs, excludedFiles)
            }
            .map { it.toPath() }
            .toList()
    }

    @Suppress("TYPE_ALIAS")
    private fun File.files(
        excludedDirs: List<File>,
        excludedFiles: List<File>,
    ): Sequence<File> = walk()
        .filter { file ->
            file.isFile && file.toPath().isKotlinCodeOrScript()
        }
        .filterNot { file ->
            file in excludedFiles || excludedDirs.any { file.startsWith(it) }
        }
}
