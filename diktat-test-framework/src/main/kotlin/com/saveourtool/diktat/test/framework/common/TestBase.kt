package com.saveourtool.diktat.test.framework.common

import com.saveourtool.diktat.test.framework.config.TestConfig
import com.saveourtool.diktat.test.framework.config.TestFrameworkProperties

/**
 * Base interface for different test runners
 */
interface TestBase {
    /**
     * simple test runner that depends on the test execution type
     *
     * @return if test failed or passed
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    fun runTest(): Boolean

    /**
     * injects test configuration that was read from .json config file
     *
     * @param testConfig json configuration
     * @param properties config from properties
     * @return test instance itself
     */
    fun initTestProcessor(testConfig: TestConfig, properties: TestFrameworkProperties): TestBase

    /**
     * @param command - command to execute in shell
     * @return execution result - in default implementation returns inputStream
     */
    fun executeCommand(command: String) = LocalCommandExecutor(command).executeCommand()
}
