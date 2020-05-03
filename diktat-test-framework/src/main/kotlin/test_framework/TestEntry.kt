package test_framework

import test_framework.config.TestArgumentsReader
import test_framework.config.TestFrameworkProperties
import test_framework.processing.TestProcessingFactory

object TestEntry {
    @JvmStatic
    fun main(args: Array<String>) {
        val properties = TestFrameworkProperties("test_framework/test_framework.properties")
        TestProcessingFactory(TestArgumentsReader(args, properties)).processTests()
    }
}