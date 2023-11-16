package com.saveourtool.diktat.cli

import com.saveourtool.diktat.DiktatRunnerArguments
import com.saveourtool.diktat.api.DiktatProcessorListener
import com.saveourtool.diktat.api.DiktatReporterCreationArguments
import com.saveourtool.diktat.api.DiktatReporterFactory
import com.saveourtool.diktat.common.config.rules.DIKTAT
import com.saveourtool.diktat.common.config.rules.DIKTAT_ANALYSIS_CONF
import com.saveourtool.diktat.util.isKotlinCodeOrScript
import com.saveourtool.diktat.util.tryToPathIfExists
import com.saveourtool.diktat.util.walkByGlob
import generated.DIKTAT_VERSION
import generated.KTLINT_VERSION
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.core.LoggerContext
import org.slf4j.event.Level
import java.io.OutputStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories
import kotlin.io.path.inputStream
import kotlin.io.path.outputStream
import kotlin.system.exitProcess
import kotlinx.cli.ArgParser
import kotlinx.cli.ArgType
import kotlinx.cli.default
import kotlinx.cli.vararg

/**
 * @param groupByFileInPlain
 * @param colorNameInPlain
 * @param logLevel
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
        val reporterArguments = DiktatReporterCreationArguments(
            id = reporterProviderId,
            outputStream = getReporterOutput(),
            groupByFileInPlain = groupByFileInPlain,
            colorNameInPlain = colorNameInPlain,
            sourceRootDir = sourceRootDir,
        )
        return DiktatRunnerArguments(
            configInputStream = Paths.get(config).inputStream(),
            sourceRootDir = sourceRootDir,
            files = getFiles(sourceRootDir),
            baselineFile = null,
            reporterArgsList = listOf(reporterArguments),
            loggingListener = loggingListener
        )
    }

    private fun getFiles(sourceRootDir: Path): Collection<Path> = patterns
        .asSequence()
        .flatMap { pattern ->
            pattern.tryToPathIfExists()?.let { sequenceOf(it) }
                ?: sourceRootDir.walkByGlob(pattern)
        }
        .filter { file -> file.isKotlinCodeOrScript() }
        .map { it.normalize() }
        .map { it.toAbsolutePath() }
        .distinct()
        .toList()

    private fun getReporterOutput(): OutputStream? = output
        ?.let { Paths.get(it) }
        ?.also { it.parent.createDirectories() }
        ?.outputStream()

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
            val reporterType: String by parser.reporterType(diktatReporterFactory)
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
            val colorName: String? by parser.colorName(diktatReporterFactory)
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

            parser.addOptionAndShowTextWithExit(
                fullName = "version",
                shortName = "V",
                description = "Output version information and exit.",
                args = args,
            ) {
                """
                    Diktat: $DIKTAT_VERSION
                    Ktlint: $KTLINT_VERSION
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
                reporterProviderId = reporterType,
                output = output,
                groupByFileInPlain = groupByFileInPlain,
                colorNameInPlain = colorName,
                logLevel = logLevel,
                patterns = patterns,
            )
        }

        /**
         * @param diktatReporterFactory
         * @return a single type of [com.saveourtool.diktat.api.DiktatReporter] as parsed cli arg
         */
        private fun ArgParser.reporterType(diktatReporterFactory: DiktatReporterFactory) = option(
            type = ArgType.Choice(
                choices = diktatReporterFactory.ids.toList(),
                toVariant = { it },
                variantToString = { it },
            ),
            fullName = "reporter",
            shortName = "r",
            description = "The reporter to use"
        )
            .default("plain")

        /**
         * @param diktatReporterFactory
         * @return a single and optional color name as parsed cli args
         */
        private fun ArgParser.colorName(diktatReporterFactory: DiktatReporterFactory) = this.option(
            type = ArgType.Choice(
                choices = diktatReporterFactory.colorNamesInPlain.toList(),
                toVariant = { it },
                variantToString = { it },
            ),
            fullName = "plain-color",
            shortName = null,
            description = "Colorize the output.",
        )

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
