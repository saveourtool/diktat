package com.huawei.test

import config.rules.RulesConfig
import config.rules.RulesConfigReader
import org.junit.Test

class JsonReaderTest {
    @Test
    fun `testing json reading`() {
        val a: List<RulesConfig>? = RulesConfigReader().readResource("src/test/resources/test-rules-config.json")
        assert(a?.filter { it.name == "CLASS_NAME_INCORRECT" && it.enabled }!!.isNotEmpty())
    }
}
