package org.cqfn.diktat.plugin.maven

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.RuleExecutionException
import com.pinterest.ktlint.reporter.plain.PlainReporter
import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugin.MojoFailureException
import org.apache.maven.plugins.annotations.Mojo
import org.apache.maven.plugins.annotations.Parameter
import org.apache.maven.project.MavenProject
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import java.io.File

/**
 * Main [Mojo] that call [DiktatRuleSetProvider]'s rules on [input] files
 * todo: extract parameter descriptions to plugin.xml
 * todo: provide editorconfig for ktlint?
 * todo: use this plugin instead of antrun
 */
@Mojo(name = "check")
class DiktatMojo : AbstractMojo() {
    /**
     * Paths that will be scanned for .kt(s) files
     */
    @Parameter(property = "diktat.inputs", defaultValue = "\${project.basedir}/src")
    var input = "\${project.basedir}/src"

    /**
     * Flag that indicates whether to turn debug logging on
     */
    @Parameter(property = "debug"/*, defaultValue = "false"*/)
    var debug = true

    /**
     * A list of [com.pinterest.ktlint.core.RuleSet] that will be used during check
     */
    private val ruleSets by lazy {
        listOf(DiktatRuleSetProvider(diktatConfigFile).get())
    }

    // todo use logger's output stream
    // todo choose reporter
    private val reporter = PlainReporter(System.out)

    /**
     * Path to diktat yml config file
     */
    @Parameter(property = "diktat.config", defaultValue = "diktat-analysis.yml")
    lateinit var diktatConfigFile: String

    /**
     * Property that can be used to access various maven settings
     */
    @Parameter(defaultValue = "\${project}", readonly = true)
    lateinit var mavenProject: MavenProject

    /**
     * Perform code check using [ruleSets]
     *
     * @throws MojoFailureException if code style check was not passed
     * @throws MojoExecutionException if [RuleExecutionException] has been thrown
     */
    override fun execute() {
        log.info("Starting diktat:check goal with inputs $input")
        File(input)
                .walk()
                .filter { file ->
                    file.isDirectory || file.extension.let { it == "kt" || it == "kts" }
                }
                .filter { it.isFile }
                .forEach { file ->
                    log.info("Checking file $file")
                    val text = file.readText()
                    val lintErrors = mutableListOf<LintError>()
                    try {
                        reporter.before(file.path)
                        KtLint.lint(
                                KtLint.Params(
                                        fileName = file.name,
                                        text = text,
                                        ruleSets = ruleSets,
                                        userData = mapOf("file_path" to file.path),
                                        script = file.extension.equals("kts", ignoreCase = true),
                                        cb = { e, isCorrected ->
                                            reporter.onLintError(file.path, e, isCorrected)
                                            lintErrors.add(e)
                                        },
                                        debug = debug
                                )
                        )
                        reporter.after(file.path)
                        if (lintErrors.isNotEmpty()) {
                            throw MojoFailureException("There are ${lintErrors.size} lint errors")
                        }
                    } catch (e: RuleExecutionException) {
                        log.error("Received exception", e)
                        throw MojoExecutionException("Error during check", e)
                    }
                }
        reporter.afterAll()
    }
}
