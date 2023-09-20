package com.saveourtool.diktat.test.framework.config

import com.saveourtool.diktat.common.cli.CliArgument
import com.saveourtool.diktat.common.config.reader.AbstractConfigReader
import io.github.oshai.kotlinlogging.KotlinLogging

import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException

import java.io.IOException
import java.io.InputStream

import kotlin.system.exitProcess
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

/**
 * Class that gives access to properties of a test
 *
 * @param classLoader [ClassLoader] which is used to load properties file
 * @property args CLI arguments
 * @property properties properties from properties file
 */
class TestArgumentsReader(
    private val args: Array<String>,
    val properties: TestFrameworkProperties,
    classLoader: ClassLoader
) : AbstractConfigReader<List<CliArgument>>() {
    private val cliArguments: List<CliArgument>? = classLoader.getResourceAsStream(properties.testFrameworkArgsRelativePath)?.let { read(it) }
    private val cmd: CommandLine by lazy { parseArguments() }

    /**
     * List of tests provided by user
     */
    val tests: List<String> by lazy {
        val tests: String? = cmd.getOptionValue("t")
        tests
            ?.split(",")
            ?.map { it.trim() }
            ?: run {
                log.error {
                    "Missing option --test or -t. Not able to run tests, please provide test names or use --all option to run all available tests"
                }
                exitProcess(2)
            }
    }
    private val declaredOptions: Options by lazy {
        val options = Options()
        cliArguments
            ?.map { it.convertToOption() }
            ?.forEach { opt -> options.addOption(opt) }
            ?: exitProcess(1)
        options
    }

    private fun parseArguments(): CommandLine {
        val parser: CommandLineParser = DefaultParser()
        val formatter = HelpFormatter()
        val options = declaredOptions
        val cmd: CommandLine
        try {
            cmd = parser.parse(options, args)
        } catch (e: ParseException) {
            log.error(e) { "Cannot parse command line arguments due to" }
            formatter.printHelp("utility-name", options)
            exitProcess(1)
        }
        return cmd
    }

    /**
     * Whether all tests should be run
     *
     * @return true if command has option "all"
     */
    fun shouldRunAllTests() = cmd.hasOption("all")

    /**
     * Parse JSON to a list of [CliArgument]s
     *
     * @param inputStream a [InputStream] representing input JSON
     * @return list of [CliArgument]s
     */
    @OptIn(ExperimentalSerializationApi::class)
    @Throws(IOException::class)
    override fun parse(inputStream: InputStream): List<CliArgument> = Json.decodeFromStream(inputStream)

    companion object {
        private val log = KotlinLogging.logger {}
    }
}
