package com.saveourtool.diktat.ruleset.config

import com.saveourtool.diktat.common.config.rules.DIKTAT_COMMON
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getCommonConfiguration
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.io.path.inputStream

class DiktatRuleConfigYamlReaderTest {
    @Test
    fun `testing json reading`() {
        val rulesConfigList: List<RulesConfig> = DiktatRuleConfigYamlReader()
            .invoke(Paths.get("src/test/resources/test-rules-config.yml").inputStream())
        assert(rulesConfigList.any { it.name == "CLASS_NAME_INCORRECT" && it.enabled })
        assert(rulesConfigList.find { it.name == "CLASS_NAME_INCORRECT" }?.configuration == emptyMap<String, String>())
        assert(rulesConfigList.find { it.name == "DIKTAT_COMMON" }
            ?.configuration?.get("domainName") == "com.saveourtool.diktat")
    }

    @Test
    fun `testing kotlin version`() {
        val rulesConfigList: List<RulesConfig> = DiktatRuleConfigYamlReader()
            .invoke(Paths.get("src/test/resources/test-rules-config.yml").inputStream())
        assert(rulesConfigList.getCommonConfiguration().kotlinVersion == kotlinVersion)
        assert(rulesConfigList.getCommonConfiguration().testAnchors.contains("androidUnitTest"))
        assert(rulesConfigList.find { it.name == DIKTAT_COMMON }
            ?.configuration
            ?.get("kotlinVersion")
            ?.kotlinVersion() == kotlinVersion)
    }

    companion object {
        private val kotlinVersion = KotlinVersion(1, 4, 21)
    }
}
