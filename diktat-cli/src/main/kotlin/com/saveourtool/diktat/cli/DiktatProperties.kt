package com.saveourtool.diktat.cli

import com.saveourtool.diktat.DIKTAT
import com.saveourtool.diktat.DIKTAT_ANALYSIS_CONF
import com.saveourtool.diktat.DiktatRunnerArguments
import com.saveourtool.diktat.ENGINE_INFO
import com.saveourtool.diktat.api.DiktatProcessorListener
import com.saveourtool.diktat.api.DiktatReporterCreationArguments
import com.saveourtool.diktat.api.DiktatReporterFactory
import com.saveourtool.diktat.api.DiktatReporterType
import com.saveourtool.diktat.util.isKotlinCodeOrScript
import com.saveourtool.diktat.util.listFiles

import generated.DIKTAT_VERSION
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.slf4j.event.Level

import java.io.OutputStream
import java.nio.file.Path
import java.nio.file.Paths

import kotlin.io.path.absolute
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.system.exitProcess
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.vararg

/**
 * @param reporterType
 * @param output
 * @param groupByFileInStdout
 * @param colorNameInStdout
 * @param logLevel
 * @property config path to `diktat-analysis.yml`
 * @property mode mode of `diktat`
 * @property patterns
 */
data class DiktatProperties(
    val config: String?,
    val mode: DiktatMode,
    private val reporterType: DiktatReporterType?,
    private val output: String?,
    private val groupByFileInStdout: Boolean?,
    private val colorNameInStdout: String?,
    private val logLevel: Level,
    val patterns: List<String>,
) {
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

    /**
     * @param sourceRootDir
     * @param loggingListener
     * @return [DiktatRunnerArguments] created from [DiktatProperties]
     */
    fun toRunnerArguments(
        sourceRootDir: Path,
        loggingListener: DiktatProcessorListener,
    ): DiktatRunnerArguments {
        val stdoutReporterCreationArguments = DiktatReporterCreationArguments(
            reporterType = DiktatReporterType.PLAIN,
            outputStream = null,
            sourceRootDir = sourceRootDir,
            groupByFileInPlain = groupByFileInStdout,
            colorNameInPlain = colorNameInStdout,
        )
        val reporterCreationArguments = reporterType?.let {
            DiktatReporterCreationArguments(
                reporterType = it,
                outputStream = getRequiredReporterOutput(),
                sourceRootDir = sourceRootDir,
            )
        }
        return DiktatRunnerArguments(
            configInputStream = config?.let { Paths.get(it).inputStream() } ?: Paths.get(DIKTAT_ANALYSIS_CONF).takeIf { it.exists() }?.inputStream(),
            sourceRootDir = sourceRootDir,
            files = getFiles(sourceRootDir),
            baselineFile = null,
            reporterArgsList = listOfNotNull(stdoutReporterCreationArguments, reporterCreationArguments),
            loggingListener = loggingListener,
        )
    }

    private fun getFiles(sourceRootDir: Path): Collection<Path> = sourceRootDir.listFiles(patterns = patterns.toTypedArray())
        .filter { file -> file.isKotlinCodeOrScript() }
        .toList()

    private fun getRequiredReporterOutput(): OutputStream = output
        ?.let { Paths.get(it).absolute() }
        ?.also { it.parent.createDirectories() }
        ?.outputStream()
        ?: throw IllegalArgumentException("A file for the reporter output is not provided")

    companion object {
        /**
         * @param diktatReporterFactory
         * @param args cli arguments
         * @return parsed [DiktatProperties]
         */
        @Suppress(
            "LongMethod",
            "TOO_LONG_FUNCTION"
        )
        fun parse(
            diktatReporterFactory: DiktatReporterFactory,
            args: Array<String>,
        ): DiktatProperties {
            val parser = ArgParser(DIKTAT)
            val config: String? by parser.config()
            val mode: DiktatMode by parser.diktatMode()
            val reporterType: DiktatReporterType? by parser.reporterType()
            val output: String? by parser.output()
            val groupByFile: Boolean? by parser.groupByFile()
            val colorName: String? by parser.colorName(diktatReporterFactory)
            val logLevel: Level by parser.logLevel()
            val patterns: List<String> by parser.argument(
                type = ArgType.String,
                description = "A list of files to process by diktat"
            ).vararg()

            parser.addOptionAndShowTextWithExit(
                fullName = "version",
                shortName = "V",
                description = "Output version information and exit.",
                args = args,
            ) {
                """
                    Diktat: $DIKTAT_VERSION
                    $ENGINE_INFO
                """.trimIndent()
            }
            parser.addOptionAndShowTextWithExit(
                fullName = "license",
                shortName = null,
                description = "Display the license and exit.",
                args = args,
            ) {
                val resourceName = "META-INF/diktat/LICENSE"
                DiktatProperties::class.java
                    .classLoader
                    .getResource(resourceName)
                    ?.readText()
                    ?: error("Resource $resourceName not found")
            }

            parser.parse(args)
            return DiktatProperties(
                config = config,
                mode = mode,
                reporterType = reporterType,
                output = output,
                groupByFileInStdout = groupByFile,
                colorNameInStdout = colorName,
                logLevel = logLevel,
                patterns = patterns,
            )
        }

        /**
         * @return a single and optional [String] for location of config as parsed cli arg
         */
        private fun ArgParser.config() = option(
            type = ArgType.String,
            fullName = "config",
            shortName = "c",
            description = "Specify the location of the YAML configuration file. By default, $DIKTAT_ANALYSIS_CONF in the current directory is used.",
        )

        /**
         * @return a single type of [DiktatMode] as parsed cli arg. [DiktatMode.CHECK] is default value
         */
        private fun ArgParser.diktatMode() = option(
            type = ArgType.Choice<DiktatMode>(),
            fullName = "mode",
            shortName = "m",
            description = "Mode of `diktat` controls that `diktat` fixes or only finds any deviations from the code style."
        ).default(DiktatMode.CHECK)

        /**
         * @return a single and optional type of [DiktatReporterType] as parsed cli arg
         */
        private fun ArgParser.reporterType() = option(
            type = ArgType.Choice<DiktatReporterType>(),
            fullName = "reporter",
            shortName = "r",
            description = "The reporter to use to log errors to output."
        )

        /**
         * @return a single and optional [String] for output as parsed cli arg
         */
        private fun ArgParser.output() = option(
            type = ArgType.String,
            fullName = "output",
            shortName = "o",
            description = "Redirect the reporter output to a file. Must be provided when the reporter is provided.",
        )

        /**
         * @return an optional flag to enable a grouping errors by files
         */
        private fun ArgParser.groupByFile() = option(
            type = ArgType.Boolean,
            fullName = "group-by-file",
            shortName = null,
            description = "A flag to group found errors by files."
        )

        /**
         * @param diktatReporterFactory
         * @return a single and optional color name as parsed cli args
         */
        private fun ArgParser.colorName(diktatReporterFactory: DiktatReporterFactory) = option(
            type = ArgType.Choice(
                choices = diktatReporterFactory.colorNamesInPlain.toList(),
                toVariant = { it },
                variantToString = { it },
            ),
            fullName = "color",
            shortName = null,
            description = "Colorize the output.",
        )

        /**
         * @return a single log leve as parser cli args. [Level.INFO] is default value
         */
        private fun ArgParser.logLevel() = option(
            type = ArgType.Choice<Level>(),
            fullName = "log-level",
            shortName = "l",
            description = "Control the log level.",
        ).default(Level.INFO)

        private fun ArgParser.addOptionAndShowTextWithExit(
            fullName: String,
            shortName: String?,
            description: String,
            args: Array<String>,
            contentSupplier: () -> String
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
                println(contentSupplier())
                exitProcess(0)
            }
        }
    }
}
