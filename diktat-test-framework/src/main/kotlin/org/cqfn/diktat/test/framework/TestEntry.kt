package org.cqfn.diktat.test.framework

import org.cqfn.diktat.test.framework.config.TestArgumentsReader
import org.cqfn.diktat.test.framework.config.TestFrameworkProperties
import org.cqfn.diktat.test.framework.processing.TestProcessingFactory

object TestEntry {
    @JvmStatic
    fun main(args: Array<String>) {
        val properties = TestFrameworkProperties("org/cqfn/diktat/test/framework/test_framework.properties")
        TestProcessingFactory(TestArgumentsReader(args, properties, javaClass.classLoader)).processTests()
    }
}
