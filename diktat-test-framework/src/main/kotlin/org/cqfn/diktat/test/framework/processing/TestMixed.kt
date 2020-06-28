package org.cqfn.diktat.test.framework.processing

import org.cqfn.diktat.test.framework.common.TestBase
import org.cqfn.diktat.test.framework.config.TestConfig
import org.cqfn.diktat.test.framework.config.TestFrameworkProperties

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
