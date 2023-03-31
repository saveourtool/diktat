package org.cqfn.diktat.cli

import org.cqfn.diktat.api.DiktatMode
import org.cqfn.diktat.api.DiktatReporter
import org.cqfn.diktat.common.config.rules.DIKTAT
import org.cqfn.diktat.common.config.rules.DIKTAT_ANALYSIS_CONF
import org.cqfn.diktat.ktlint.DiktatReporterFactoryImpl
import org.cqfn.diktat.util.colorName
import org.cqfn.diktat.util.reporterProviderId
import com.pinterest.ktlint.core.Reporter
import com.pinterest.ktlint.core.ReporterProvider
import com.pinterest.ktlint.reporter.checkstyle.CheckStyleReporterProvider
import com.pinterest.ktlint.reporter.html.HtmlReporterProvider
import com.pinterest.ktlint.reporter.json.JsonReporterProvider
import com.pinterest.ktlint.reporter.plain.PlainReporterProvider
import com.pinterest.ktlint.reporter.plain.internal.Color
import com.pinterest.ktlint.reporter.sarif.SarifReporterProvider
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.slf4j.event.Level
import java.io.PrintStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream
import kotlin.system.exitProcess
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.vararg

/**
 * @property config path to `diktat-analysis.yml`
 * @property mode mode of `diktat`
 * @property reporterProviderId
 * @property output
 * @property patterns
 */
data class DiktatProperties(
    val config: String,
    val mode: DiktatMode,
    val reporterProviderId: String,
    val output: String?,
    private val groupByFileInPlain: Boolean,
    private val colorNameInPlain: String?,
    private val logLevel: Level,
    val patterns: List<String>,
) {
    /**
     * @param sourceRootDir
     * @return a configured [Reporter]
     */
    fun reporter(sourceRootDir: Path): DiktatReporter = buildReporter(
        reporterProviderId, output, colorNameInPlain, groupByFileInPlain, sourceRootDir
    )

    /**
     * Configure logger level using [logLevel]
     */
    fun configureLogger() {
        // set log level
        LogManager.getContext(false)
            .let { it as LoggerContext }
            .also { ctx ->
                ctx.configuration.rootLogger.level = when (logLevel) {
                    Level.ERROR -> org.apache.logging.log4j.Level.ERROR
                    Level.WARN -> org.apache.logging.log4j.Level.WARN
                    Level.INFO -> org.apache.logging.log4j.Level.INFO
                    Level.DEBUG -> org.apache.logging.log4j.Level.DEBUG
                    Level.TRACE -> org.apache.logging.log4j.Level.TRACE
                }
            }
            .updateLoggers()
    }

    companion object {
        /**
         * @param args cli arguments
         * @return parsed [DiktatProperties]
         */
        @Suppress(
            "LongMethod",
            "TOO_LONG_FUNCTION"
        )
        fun parse(args: Array<String>): DiktatProperties {
            val parser = ArgParser(DIKTAT)
            val config: String by parser.option(
                type = ArgType.String,
                fullName = "config",
                shortName = "c",
                description = "Specify the location of the YAML configuration file. By default, $DIKTAT_ANALYSIS_CONF in the current directory is used.",
            ).default(DIKTAT_ANALYSIS_CONF)
            val mode: DiktatMode by parser.option(
                type = ArgType.Choice<DiktatMode>(),
                fullName = "mode",
                shortName = "m",
                description = "Mode of `diktat` controls that `diktat` fixes or only finds any deviations from the code style."
            ).default(DiktatMode.CHECK)
            val reporterProviderId: String by parser.reporterProviderId()
            val output: String? by parser.option(
                type = ArgType.String,
                fullName = "output",
                shortName = "o",
                description = "Redirect the reporter output to a file.",
            )
            val groupByFileInPlain: Boolean by parser.option(
                type = ArgType.Boolean,
                fullName = "plain-group-by-file",
                shortName = null,
                description = "A flag for plain reporter"
            ).default(false)
            val colorName: String? by parser.colorName()
            val logLevel: Level by parser.option(
                type = ArgType.Choice<Level>(),
                fullName = "log-level",
                shortName = "l",
                description = "Enable the output with specific level",
            ).default(Level.INFO)
            val patterns: List<String> by parser.argument(
                type = ArgType.String,
                description = "A list of files to process by diktat"
            ).vararg()

            parser.addOptionAndShowResourceWithExit(
                fullName = "version",
                shortName = "V",
                description = "Output version information and exit.",
                args = args,
                resourceName = "META-INF/diktat/version"
            )
            parser.addOptionAndShowResourceWithExit(
                fullName = "license",
                shortName = null,
                description = "Display the license and exit.",
                args = args,
                resourceName = "META-INF/diktat/LICENSE",
            )

            parser.parse(args)
            return DiktatProperties(
                config = config,
                mode = mode,
                reporterProviderId = reporterProviderId,
                output = output,
                groupByFileInPlain = groupByFileInPlain,
                colorNameInPlain = colorName,
                logLevel = logLevel,
                patterns = patterns,
            )
        }

        private fun ArgParser.addOptionAndShowResourceWithExit(
            fullName: String,
            shortName: String?,
            description: String,
            args: Array<String>,
            resourceName: String,
        ) {
            // add here to print in help
            option(
                type = ArgType.Boolean,
                fullName = fullName,
                shortName = shortName,
                description = description
            )
            if (args.contains("--$fullName") || shortName?.let { args.contains("-$it") } == true) {
                @Suppress("DEBUG_PRINT", "ForbiddenMethodCall")
                print(readFromResource(resourceName))
                exitProcess(0)
            }
        }

        private fun readFromResource(resourceName: String): String = DiktatProperties::class.java
            .classLoader
            .getResource(resourceName)
            ?.readText()
            ?: error("Resource $resourceName not found")

        private fun buildReporter(
            reporterProviderId: String,
            output: String?,
            colorNameInPlain: String?,
            groupByFileInPlain: Boolean,
            sourceRootDir: Path,
        ): DiktatReporter {
            val outputStream = output
                ?.let { Paths.get(it) }
                ?.also { it.parent.createDirectories() }
                ?.outputStream()
                ?.let { PrintStream(it) }
                ?: System.out
            return DiktatReporterFactoryImpl().invoke(
                reporterProviderId,
                outputStream,
                buildMap<String, Any> {
                    colorNameInPlain?.let {
                        require(reporterProviderId == "plain") {
                            "colorization is applicable only for plain reporter"
                        }
                        put("color", true)
                        put("color_name", it)
                    } ?: run {
                        put("color", false)
                        put("color_name", Color.DARK_GRAY.name)
                    }
                    if (groupByFileInPlain) {
                        require(reporterProviderId == "plain") {
                            "groupByFile is applicable only for plain reporter"
                        }
                        put("group_by_file", true)
                    }
                }.mapValues { it.value.toString() },
                sourceRootDir,
            )
        }


        /**
         * supported color names in __KtLint__, taken from [Color]
         */
        val colorNamesForPlainReporter = Color.values().map { it.name }

        /**
         * A default [ReporterProvider] for [PlainReporterProvider]
         */
        val plainReporterProvider = PlainReporterProvider()

        /**
         * All [ReporterProvider] which __KtLint__ provides
         */
        val reporterProviders = setOf(
            plainReporterProvider,
            JsonReporterProvider(),
            SarifReporterProvider(),
            CheckStyleReporterProvider(),
            HtmlReporterProvider(),
        )
            .associateBy { it.id }

        /**
         * @return true if receiver is [PlainReporterProvider]
         */
        internal fun ReporterProvider<*>.isPlain(): Boolean = id == plainReporterProvider.id
    }
}
