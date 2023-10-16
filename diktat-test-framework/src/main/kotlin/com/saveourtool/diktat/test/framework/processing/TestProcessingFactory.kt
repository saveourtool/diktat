package com.saveourtool.diktat.test.framework.processing

import com.saveourtool.diktat.test.framework.common.TestBase
import com.saveourtool.diktat.test.framework.config.TestArgumentsReader
import com.saveourtool.diktat.test.framework.config.TestConfig
import com.saveourtool.diktat.test.framework.config.TestConfig.ExecutionType
import com.saveourtool.diktat.test.framework.config.TestConfigReader
import io.github.oshai.kotlinlogging.KotlinLogging

import java.io.File
import java.io.IOException
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Stream

import kotlin.system.exitProcess

/**
 * A class that runs tests based on configuration
 *
 * @param argReader
 */
@Suppress("ForbiddenComment")
class TestProcessingFactory(private val argReader: TestArgumentsReader) {
    private val allTestsFromResources: List<String> by lazy {
        val fileUrl: URL? = javaClass.getResource("/${argReader.properties.testConfigsRelativePath}")
        val resource = fileUrl
            ?.let { File(it.file) }
            ?: run {
                log.error {
                    "Not able to get directory with test configuration files: ${argReader.properties.testConfigsRelativePath}"
                }
                exitProcess(STATUS_FIVE)
            }
        try {
            resource
                .walk()
                .filter { file -> file.isFile }
                .map { file -> file.name.replace(".json", "") }
                .toList()
        } catch (e: IOException) {
            log.error(e) { "Got -all option, but cannot read config files " }
            exitProcess(STATUS_THREE)
        }
    }

    /**
     * Run all tests specified in input parameters and log results
     */
    fun processTests() {
        val failedTests = AtomicInteger(0)
        val passedTests = AtomicInteger(0)
        val testList: List<String> = if (argReader.shouldRunAllTests()) {
            log.info { "Will run all available test cases" }
            allTestsFromResources
        } else {
            log.info { "Will run specific tests: ${argReader.tests}" }
            argReader.tests
        }

        val testStream: Stream<String> =
            if (argReader.properties.isParallelMode) testList.parallelStream() else testList.stream()

        testStream
            .map { test: String -> findTestInResources(test) }
            .filter { it != null }
            .map { it as TestConfig }
            .forEach { test: TestConfig ->
                if (processTest(test)) passedTests.incrementAndGet() else failedTests.incrementAndGet()
            }

        log.info { "Test processing finished. Passed tests: [$passedTests]. Failed tests: [$failedTests]" }
    }

    private fun findTestInResources(test: String): TestConfig? =
        TestConfigReader("${argReader.properties.testConfigsRelativePath}/$test.json", javaClass.classLoader)
            .config
            ?.setTestName(test)

    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    private fun processTest(testConfig: TestConfig): Boolean {
        val test: TestBase = when (testConfig.executionType) {
            ExecutionType.MIXED ->
                // FixMe: support Mixed type
                TestCompare()
            ExecutionType.COMPARE -> TestCompare()
            ExecutionType.CHECK_WARN -> TestCheckWarn()
        }

        return test.initTestProcessor(testConfig, argReader.properties)
            .runTest()
    }

    companion object {
        private val log = KotlinLogging.logger {}
        private const val STATUS_FIVE = 5
        private const val STATUS_THREE = 3
    }
}
