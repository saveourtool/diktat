package org.cqfn.diktat.plugin.maven

import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleExecutionException
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.reporter.plain.PlainReporter
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject

import java.io.File

/**
 * Base [Mojo] for checking and fixing code using diktat
 */
abstract class DiktatBaseMojo : AbstractMojo() {
    /**
     * Flag that indicates whether to turn debug logging on
     */
    @Parameter(property = "diktat.debug")
    var debug = false

    // FixMe: Reporter should be chosen via plugin configuration
    private val reporter = PlainReporter(System.out)

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
     * @param params instance of [KtLint.Params] used in analysis
     */
    abstract fun runAction(params: KtLint.Params)

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
            listOf(DiktatRuleSetProvider(configFile).get())
        }
        val lintErrors: MutableList<LintError> = mutableListOf()

        inputs
            .map(::File)
            .forEach {
                checkDirectory(it, lintErrors, ruleSets)
            }

        reporter.afterAll()
        if (lintErrors.isNotEmpty()) {
            throw MojoFailureException("There are ${lintErrors.size} lint errors")
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
    private fun checkDirectory(
        directory: File,
        lintErrors: MutableList<LintError>,
        ruleSets: Iterable<RuleSet>
    ) {
        val (excludedDirs, excludedFiles) = excludes.map(::File).partition { it.isDirectory }
        directory
            .walk()
            .filter { file ->
                file.isDirectory || file.extension.let { it == "kt" || it == "kts" }
            }
            .filter { it.isFile }
            .filterNot { file -> file in excludedFiles || excludedDirs.any { file.startsWith(it) } }
            .forEach { file ->
                log.debug("Checking file $file")
                try {
                    reporter.before(file.path)
                    checkFile(file, lintErrors, ruleSets)
                    reporter.after(file.path)
                } catch (e: RuleExecutionException) {
                    log.error("Unhandled exception during rule execution: ", e)
                    throw MojoExecutionException("Unhandled exception during rule execution", e)
                }
            }
    }

    private fun checkFile(file: File,
                          lintErrors: MutableList<LintError>,
                          ruleSets: Iterable<RuleSet>
    ) {
        val text = file.readText()
        val params =
                KtLint.Params(
                    fileName = file.relativeTo(mavenProject.basedir).path,
                    text = text,
                    ruleSets = ruleSets,
                    userData = mapOf("file_path" to file.path),
                    script = file.extension.equals("kts", ignoreCase = true),
                    cb = { lintError, isCorrected ->
                        reporter.onLintError(file.path, lintError, isCorrected)
                        lintErrors.add(lintError)
                    },
                    debug = debug
                )
        runAction(params)
    }
}
