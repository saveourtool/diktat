package com.huawei.test

import config.rules.RulesConfig
import config.rules.RulesConfigReader
import org.junit.Test

class JsonReaderTest {
    @Test
    fun `testing json reading`() {
        val a: List<RulesConfig>? = RulesConfigReader().readResource("src/test/resources/test-rules-config.json")
        assert(a?.filter { it.name == "CLASS_NAME_INCORRECT" && it.enabled }!!.isNotEmpty())
        assert(a.find { it.name == "CLASS_NAME_INCORRECT" }?.configuration == mapOf<String, String>())
        assert(a.find { it.name == "HEADER_MISSING_OR_WRONG_COPYRIGHT" }!!.configuration == mapOf("isCopyrightMandatory" to "true"))
    }
}
