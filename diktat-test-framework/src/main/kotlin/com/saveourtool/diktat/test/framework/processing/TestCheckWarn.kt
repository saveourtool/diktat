package com.saveourtool.diktat.test.framework.processing

import com.saveourtool.diktat.test.framework.config.TestConfig
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging

/**
 * [TestCompare] that uses stderr as tests output stream
 */
class TestCheckWarn : TestCompare() {
    @Suppress("MISSING_KDOC_CLASS_ELEMENTS")
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
