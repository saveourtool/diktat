package com.saveourtool.diktat.test.framework

import com.saveourtool.diktat.test.framework.config.TestArgumentsReader
import com.saveourtool.diktat.test.framework.config.TestFrameworkProperties
import com.saveourtool.diktat.test.framework.processing.TestProcessingFactory

/**
 * Main entry point for test executions
 */
object TestEntry {
    @JvmStatic
    fun main(args: Array<String>) {
        val properties = TestFrameworkProperties("com/saveourtool/diktat/test/framework/test_framework.properties")
        TestProcessingFactory(TestArgumentsReader(args, properties, javaClass.classLoader)).processTests()
    }
}
