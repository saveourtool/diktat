package org.cqfn.diktat.test.framework.processing

import org.cqfn.diktat.test.framework.config.TestConfig
import mu.KLogger
import mu.KotlinLogging

/**
 * [TestCompare] that uses stderr as tests output stream
 */
class TestCheckWarn : TestCompare() {
    @Suppress("EMPTY_BLOCK_STRUCTURE_ERROR", "MISSING_KDOC_CLASS_ELEMENTS")
    override val log: KLogger = KotlinLogging.logger {}

    @Suppress(
        "UnusedPrivateMember",
        "UNUSED",
        "VarCouldBeVal"
    )
    private var testConfig: TestConfig? = null

    /**
     * Get tests execution result
     */
    override fun getExecutionResult() = testResult.stdErr
}
