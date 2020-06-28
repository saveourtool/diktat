package org.cqfn.diktat.test.framework.common

import org.cqfn.diktat.test.framework.config.TestConfig
import org.cqfn.diktat.test.framework.config.TestFrameworkProperties

interface TestBase {
    /**
     * simple test runner that depends on the test execution type
     * @return if test failed or passed
     */
    fun runTest(): Boolean

    /**
     * injects test configuration that was read from .json config file
     * @return test instance itself
     */
    fun initTestProcessor(testConfig: TestConfig?, properties: TestFrameworkProperties?): TestBase?

    /**
     *
     * @param command - command to execute in shell
     * @return - execution result - in default implementation returns inputStream
     */
    fun executeCommand(command: String?): ExecutionResult? {
        return LocalCommandExecutor(command!!).executeCommand()
    }
}
