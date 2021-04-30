package org.cqfn.diktat.test.framework.processing

import org.cqfn.diktat.test.framework.config.TestConfig
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * [TestCompare] that uses stderr as tests output stream
 */
class TestCheckWarn : TestCompare() {
    @Suppress("MISSING_KDOC_CLASS_ELEMENTS") override val log: Logger = LoggerFactory.getLogger(TestCheckWarn::class.java)
    @Suppress("UnusedPrivateMember", "UNUSED") private var testConfig: TestConfig? = null

    /**
     * Get tests execution result
     */
    override fun getExecutionResult() = testResult.stdErr
}
