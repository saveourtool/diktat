package test_framework.processing

import test_framework.common.TestBase
import test_framework.config.TestConfig
import test_framework.config.TestFrameworkProperties

class TestMixed : TestBase {
    private var testConfig: TestConfig? = null
    override fun runTest(): Boolean {
        return true
    }

    override fun initTestProcessor(testConfig: TestConfig?, properties: TestFrameworkProperties?): TestMixed? {
        this.testConfig = testConfig
        return this
    }
}