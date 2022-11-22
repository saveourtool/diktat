package org.cqfn.diktat.cli

import org.cqfn.diktat.api.DiktatMode
import org.cqfn.diktat.common.config.rules.DIKTAT
import org.cqfn.diktat.common.config.rules.DIKTAT_ANALYSIS_CONF
import org.cqfn.diktat.ktlint.isPlain
import org.cqfn.diktat.ktlint.reporterProvider
import com.pinterest.ktlint.core.Reporter
import com.pinterest.ktlint.core.ReporterProvider
import com.pinterest.ktlint.reporter.plain.internal.Color
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.slf4j.event.Level
import java.io.PrintStream
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.outputStream
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.vararg

/**
 * @property config path to `diktat-analysis.yml`
 * @property mode mode of `diktat`
 * @property reporter
 * @property output
 * @property logLevel
 */
@Suppress("DEPRECATION")
data class DiktatProperties(
    val config: String,
    val mode: DiktatMode,
    val reporterProvider: ReporterProvider<*>,
    val output: String?,
    val groupByFileInPlain: Boolean,
    val colorInPlain: Color?,
    val logLevel: Level,
    val patterns: List<String>,
) {
    /**
     * @return a configured [Reporter]
     */
    fun reporter(): Reporter {
        return reporterProvider.get(
            out = output
                ?.let { Paths.get(it) }
                ?.also { it.parent.createDirectories() }
                ?.outputStream()
                ?.let { PrintStream(it) }
                ?: System.out,
            opt = buildMap<String, Any> {
                colorInPlain?.let {
                    require(reporterProvider.isPlain()) {
                        "colorization is applicable only for plain reporter"
                    }
                    put("color", true)
                    put("color_name", it)
                } ?: run {
                    put("color", false)
                }
                put("format", (mode == DiktatMode.FIX))
                if (groupByFileInPlain) {
                    require(reporterProvider.isPlain()) {
                        "groupByFile is applicable only for plain reporter"
                    }
                    put("group_by_file", true)
                }
            }.mapValues { it.toString() },
        )
    }

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
        fun parse(args: Array<String>): DiktatProperties {
            val parser = ArgParser(DIKTAT)
            val config: String by parser.option(
                type = ArgType.String,
                fullName = "config",
                shortName = "c",
                description = """
            Specify the location of the YAML configuration file.
            By default, $DIKTAT_ANALYSIS_CONF in the current
            directory is used.
        """.trimIndent(),
            ).default(DIKTAT_ANALYSIS_CONF)
            val mode: DiktatMode by parser.option(
                type = ArgType.Choice<DiktatMode>(),
                fullName = "mode",
                shortName = "m",
                description = "Mode of `diktat` controls that `diktat` fixes or only finds any deviations from the code style."
            ).default(DiktatMode.CHECK)
            val reporterProvider: ReporterProvider<*> = parser.reporterProvider()
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
            val color: Color? by parser.option(
                type = ArgType.Choice(),
                fullName = "plain-color",
                shortName = null,
                description = "Colorize the output."
            )
            val logLevel: Level by parser.option(
                type = ArgType.Choice<Level>(),
                fullName = "log-level",
                shortName = "l",
                description = "Enable the output with specific level",
            ).default(Level.INFO)
            val patterns: List<String> by parser.argument(
                type = ArgType.String,
                description = ""
            )
                .vararg()

            parser.parse(args)
            return DiktatProperties(
                config = config,
                mode = mode,
                reporterProvider = reporterProvider,
                output = output,
                groupByFileInPlain = groupByFileInPlain,
                colorInPlain = color,
                logLevel = logLevel,
                patterns = patterns,
            )
        }
    }
}

