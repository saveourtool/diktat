package org.cqfn.diktat.test.framework.processing

import org.apache.commons.io.FileUtils
import org.cqfn.diktat.test.framework.common.ExecutionResult
import org.cqfn.diktat.test.framework.common.TestBase
import org.cqfn.diktat.test.framework.config.TestConfig
import org.cqfn.diktat.test.framework.config.TestFrameworkProperties
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

@Suppress("ForbiddenComment")
open class TestCompare : TestBase {
    protected open val log: Logger = LoggerFactory.getLogger(TestCompare::class.java)
    private lateinit var expectedResult: File

    // testResultFile will be used if and only if --in-place option will be used
    private lateinit var testFile: File
    private lateinit var testConfig: TestConfig
    protected lateinit var testResult: ExecutionResult

    override fun runTest(): Boolean {
        // FixMe: this is an execution for Windows, should support other OS
        val testPassed = if (testConfig.inPlace) processInPlace() else processToStdOut()

        if (testPassed) {
            log.info("Test <${testConfig.testName}> passed")
        } else {
            log.error("Test <${testConfig.testName}> failed")
        }

        return testPassed
    }

    override fun initTestProcessor(testConfig: TestConfig, properties: TestFrameworkProperties): TestCompare {
        this.testConfig = testConfig
        this.expectedResult = buildFullPathToResource(
                testConfig.expectedResultFile,
                properties.testFilesRelativePath
        )
        this.testFile = buildFullPathToResource(testConfig.testFile, properties.testFilesRelativePath)

        return this
    }

    private fun processInPlace(): Boolean {
        val copyTestFile = File("${testFile}_copy")
        FileUtils.copyFile(testFile, copyTestFile)
        executeCommand("cmd /c " + testConfig.executionCommand + " " + copyTestFile)

        val testPassed = FileComparator(expectedResult, copyTestFile).compareFilesEqual()
        FileUtils.forceDelete(copyTestFile)

        return testPassed
    }

    private fun processToStdOut(): Boolean {
        this.testResult = executeCommand("cmd /c " + testConfig.executionCommand + " " + testFile)

        return FileComparator(expectedResult, getExecutionResult()).compareFilesEqual()
    }

    private fun buildFullPathToResource(resourceFile: String, resourceAbsolutePath: String): File {
        val fileURL = javaClass.classLoader.getResource("$resourceAbsolutePath/$resourceFile")
        require(fileURL != null) { "Cannot read resource file $$resourceAbsolutePath/$resourceFile - it cannot be found in resources" }
        return File(fileURL.file)
    }

    protected open fun getExecutionResult() = testResult.stdOut
}
