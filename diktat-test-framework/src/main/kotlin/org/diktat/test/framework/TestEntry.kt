package org.diktat.test.framework

import org.diktat.test.framework.config.TestArgumentsReader
import org.diktat.test.framework.config.TestFrameworkProperties
import org.diktat.test.framework.processing.TestProcessingFactory

object TestEntry {
    @JvmStatic
    fun main(args: Array<String>) {
        val properties = TestFrameworkProperties("org/diktat/test/framework/test_framework.properties")
        TestProcessingFactory(TestArgumentsReader(args, properties)).processTests()
    }
}
