@file:Suppress(
    "Deprecation"
)

package org.cqfn.diktat.plugin.maven

import org.cqfn.diktat.ruleset.rules.DiktatRuleSetFactory
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import org.cqfn.diktat.ruleset.utils.isKotlinCodeOrScript

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Reporter
import com.pinterest.ktlint.core.RuleExecutionException
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.internal.CurrentBaseline
import com.pinterest.ktlint.core.internal.containsLintError
import com.pinterest.ktlint.core.internal.loadBaseline
import com.pinterest.ktlint.reporter.baseline.BaselineReporter
import com.pinterest.ktlint.reporter.html.HtmlReporter
import com.pinterest.ktlint.reporter.json.JsonReporter
import com.pinterest.ktlint.reporter.plain.PlainReporter
import com.pinterest.ktlint.reporter.sarif.SarifReporter
import org.apache.maven.execution.MavenSession
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.Mojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

import java.io.File
import java.io.FileOutputStream
import java.io.PrintStream

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
    private lateinit var reporterImpl: Reporter

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
     * @param params instance of [KtLint.ExperimentalParams] used in analysis
     */
    abstract fun runAction(params: KtLint.ExperimentalParams)

    /**
     * Perform code check using diktat ruleset
     *
     * @throws MojoFailureException if code style check was not passed
     * @throws MojoExecutionException if [RuleExecutionException] has been thrown
     */
    override fun execute() {
        val configFile = resolveConfig()
        if (!File(configFile).exists()) {
            throw MojoExecutionException("Configuration file $diktatConfigFile doesn't exist")
        }
        log.info("Running diKTat plugin with configuration file $configFile and inputs $inputs" +
                if (excludes.isNotEmpty()) " and excluding $excludes" else ""
        )

        val ruleSets by lazy {
            listOf(DiktatRuleSetProvider(DiktatRuleSetFactory(configFile)).get())
        }
        val baselineResults = baseline?.let { loadBaseline(it.absolutePath) }
            ?: CurrentBaseline(emptyMap(), false)
        reporterImpl = resolveReporter(baselineResults)
        reporterImpl.beforeAll()
        val lintErrors: MutableList<LintError> = mutableListOf()

        inputs
            .map(::File)
            .forEach {
                checkDirectory(it, lintErrors, baselineResults.baselineRules ?: emptyMap(), ruleSets)
            }

        reporterImpl.afterAll()
        if (lintErrors.isNotEmpty()) {
            throw MojoFailureException("There are ${lintErrors.size} lint errors")
        }
    }

    private fun resolveReporter(baselineResults: CurrentBaseline): Reporter {
        val output = if (this.output.isBlank()) {
            if (this.githubActions) {
                // need to set user.home specially for ktlint, so it will be able to put a relative path URI in SARIF
                System.setProperty("user.home", mavenSession.executionRootDirectory)
                PrintStream(FileOutputStream("${mavenProject.basedir}/${mavenProject.name}.sarif", false))
            } else {
                System.`out`
            }
        } else {
            PrintStream(FileOutputStream(this.output, false))
        }

        val actualReporter = if (this.githubActions) {
            SarifReporter(output)
        } else {
            when (this.reporter) {
                "sarif" -> SarifReporter(output)
                "plain" -> PlainReporter(output)
                "json" -> JsonReporter(output)
                "html" -> HtmlReporter(output)
                else -> {
                    log.warn("Reporter name ${this.reporter} was not specified or is invalid. Falling to 'plain' reporter")
                    PlainReporter(output)
                }
            }
        }

        return if (baselineResults.baselineGenerationNeeded) {
            val baselineReporter = BaselineReporter(PrintStream(FileOutputStream(baseline, true)))
            return Reporter.from(actualReporter, baselineReporter)
        } else {
            actualReporter
        }
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

    /**
     * @throws MojoExecutionException if [RuleExecutionException] has been thrown by ktlint
     */
    @Suppress("TYPE_ALIAS")
    private fun checkDirectory(
        directory: File,
        lintErrors: MutableList<LintError>,
        baselineRules: Map<String, List<LintError>>,
        ruleSets: Iterable<RuleSet>
    ) {
        val (excludedDirs, excludedFiles) = excludes.map(::File).partition { it.isDirectory }
        directory
            .walk()
            .filter { file ->
                file.isDirectory || file.toPath().isKotlinCodeOrScript()
            }
            .filter { it.isFile }
            .filterNot { file -> file in excludedFiles || excludedDirs.any { file.startsWith(it) } }
            .forEach { file ->
                log.debug("Checking file $file")
                try {
                    reporterImpl.before(file.absolutePath)
                    checkFile(
                        file,
                        lintErrors,
                        baselineRules.getOrDefault(
                            file.relativeTo(mavenProject.basedir.parentFile).invariantSeparatorsPath,
                            emptyList()
                        ),
                        ruleSets
                    )
                    reporterImpl.after(file.absolutePath)
                } catch (e: RuleExecutionException) {
                    log.error("Unhandled exception during rule execution: ", e)
                    throw MojoExecutionException("Unhandled exception during rule execution", e)
                }
            }
    }

    private fun checkFile(file: File,
                          lintErrors: MutableList<LintError>,
                          baselineErrors: List<LintError>,
                          ruleSets: Iterable<RuleSet>
    ) {
        val text = file.readText()
        val params =
            KtLint.ExperimentalParams(
                fileName = file.absolutePath,
                text = text,
                ruleSets = ruleSets,
                script = file.extension.equals("kts", ignoreCase = true),
                cb = { lintError, isCorrected ->
                    if (!baselineErrors.containsLintError(lintError)) {
                        reporterImpl.onLintError(file.absolutePath, lintError, isCorrected)
                        lintErrors.add(lintError)
                    }
                },
                debug = debug
            )
        runAction(params)
    }
}
