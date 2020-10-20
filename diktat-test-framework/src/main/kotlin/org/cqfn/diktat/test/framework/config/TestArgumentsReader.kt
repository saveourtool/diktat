package org.cqfn.diktat.test.framework.config

import java.io.BufferedReader
import java.io.IOException
import java.util.stream.Collectors
import kotlin.system.exitProcess
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.commons.cli.CommandLine
import org.apache.commons.cli.CommandLineParser
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.cqfn.diktat.common.cli.CliArgument
import org.cqfn.diktat.common.config.reader.JsonResourceConfigReader
import org.slf4j.LoggerFactory

/**
 * Class that gives access to properties of a test
 *
 * @property args CLI arguments
 * @property properties properties from properties file
 * @property classLoader [ClassLoader] which is used to load properties file
 */
class TestArgumentsReader(
        private val args: Array<String>,
        val properties: TestFrameworkProperties,
        override val classLoader: ClassLoader
) : JsonResourceConfigReader<List<CliArgument>?>() {
    private val cliArguments: List<CliArgument>? = readResource(properties.testFrameworkArgsRelativePath)
    private val cmd: CommandLine

    /**
     * List of tests provided by user
     */
    val tests: List<String>
        get() {
            val tests = cmd.getOptionValue("t")
            if (tests == null) {
                log.error("""Missing option --test or -t. Not able to run tests, please provide test names or use --all
                         option to run all available tests""")
                exitProcess(2)
            }
            return tests
                    .split(",")
                    .map { it.trim() }
        }

    private val declaredOptions: Options
        get() {
            val options = Options()
            if (cliArguments != null) {
                cliArguments
                        .map { it.convertToOption() }
                        .forEach { opt -> options.addOption(opt) }
            } else {
                exitProcess(1)
            }
            return options
        }

    init {
        cmd = parseArguments()
    }

    private fun parseArguments(): CommandLine {
        val parser: CommandLineParser = DefaultParser()
        val formatter = HelpFormatter()
        val options = declaredOptions
        val cmd: CommandLine
        try {
            cmd = parser.parse(options, args)
        } catch (e: ParseException) {
            log.error("Cannot parse command line arguments due to ", e)
            formatter.printHelp("utility-name", options)
            exitProcess(1)
        }
        return cmd
    }

    /**
     * Whether all tests should be run
     */
    fun shouldRunAllTests() = cmd.hasOption("all")

    /**
     * Parse JSON to a list of [CliArgument]s
     *
     * @param fileStream a [BufferedReader] representing input JSON
     * @return list of [CliArgument]s
     */
    @Throws(IOException::class)
    override fun parseResource(fileStream: BufferedReader): List<CliArgument> {
        val jsonValue = fileStream.lines().collect(Collectors.joining())
        return Json.decodeFromString<List<CliArgument>>(jsonValue)
    }

    companion object {
        private val log = LoggerFactory.getLogger(TestArgumentsReader::class.java)
    }
}
