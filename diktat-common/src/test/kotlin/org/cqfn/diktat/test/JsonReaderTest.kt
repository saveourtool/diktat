package org.cqfn.diktat.test

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.junit.jupiter.api.Test

class JsonReaderTest {
    @Test
    fun `testing json reading`() {
        val rulesConfigList: List<RulesConfig> = RulesConfigReader(javaClass.classLoader).readResource("src/test/resources/test-rules-config.json")!!
        assert(rulesConfigList.any { it.name == "CLASS_NAME_INCORRECT" && it.enabled })
        assert(rulesConfigList.find { it.name == "CLASS_NAME_INCORRECT" }?.configuration == mapOf<String, String>())
        assert(rulesConfigList.find { it.name == "HEADER_MISSING_OR_WRONG_COPYRIGHT" }
                ?.configuration == mapOf("isCopyrightMandatory" to "true"))
    }
}
