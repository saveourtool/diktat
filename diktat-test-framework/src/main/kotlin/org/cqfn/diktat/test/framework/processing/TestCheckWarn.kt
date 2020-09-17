package org.cqfn.diktat.test.framework.processing

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.cqfn.diktat.test.framework.config.TestConfig

class TestCheckWarn : TestCompare() {
    override val log: Logger = LoggerFactory.getLogger(TestCheckWarn::class.java)

    private var testConfig: TestConfig? = null

    override fun getExecutionResult() = testResult.stdErr
}
