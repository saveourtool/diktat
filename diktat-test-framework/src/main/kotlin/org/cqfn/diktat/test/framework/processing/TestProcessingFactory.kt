package org.cqfn.diktat.test.framework.processing

import org.slf4j.LoggerFactory
import org.cqfn.diktat.test.framework.common.TestBase
import org.cqfn.diktat.test.framework.config.TestArgumentsReader
import org.cqfn.diktat.test.framework.config.TestConfig
import org.cqfn.diktat.test.framework.config.TestConfig.ExecutionType
import org.cqfn.diktat.test.framework.config.TestConfigReader
import java.io.File
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger
import java.util.stream.Stream
import kotlin.system.exitProcess

@Suppress("ForbiddenComment")
class TestProcessingFactory(private val argReader: TestArgumentsReader) {
    companion object {
        private val log = LoggerFactory.getLogger(TestProcessingFactory::class.java)
    }

    fun processTests() {
        val failedTests = AtomicInteger(0)
        val passedTests = AtomicInteger(0)
        val testList: List<String>? = if (argReader.shouldRunAllTests()) {
            log.info("Will run all available test cases")
            allTestsFromResources
        } else {
            log.info("Will run specific tests: ${argReader.tests}")
            argReader.tests
        }

        val testStream: Stream<String> =
                if (argReader.properties.isParallelMode) testList!!.parallelStream() else testList!!.stream()

        testStream.map { test: String -> findTestInResources(test) }
                .forEach { test: TestConfig ->
                    if (processTest(test)) passedTests.incrementAndGet() else failedTests.incrementAndGet()
                }

        log.info("Test processing finished. Passed tests: [${passedTests}]. Failed tests: [${failedTests}]")
    }

    private fun findTestInResources(test: String): TestConfig =
            TestConfigReader("${argReader.properties.testConfigsRelativePath}/$test.json")
                    .config!!
                    .setTestName(test)


    private val allTestsFromResources: List<String>?
        get() {
            val fileURL = javaClass.getResource("/${argReader.properties.testConfigsRelativePath}")
            if (fileURL == null) {
                log.error("Not able to get directory with test configuration files: " +
                        argReader.properties.testConfigsRelativePath)
                exitProcess(5)
            }
            val resource = File(fileURL.file)
            try {
                return resource.walk()
                        .filter { file -> file.isFile }
                        .map { file -> file.name.replace(".json", "") }
                        .toList()
            } catch (e: IOException) {
                log.error("Got -all option, but cannot read config files ", e)
                exitProcess(3)
            }
        }

    private fun processTest(testConfig: TestConfig): Boolean {
        val test: TestBase = when (testConfig.executionType) {
            ExecutionType.MIXED ->
                // FixMe: support Mixed type
                TestCompare()
            ExecutionType.COMPARE -> TestCompare()
            ExecutionType.CHECK_WARN -> TestCheckWarn()
        }

        return test.initTestProcessor(testConfig, argReader.properties)!!
                .runTest()
    }
}
