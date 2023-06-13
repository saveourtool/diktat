package com.saveourtool.diktat.test

import com.saveourtool.diktat.common.config.rules.DIKTAT_COMMON
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.RulesConfigReader
import com.saveourtool.diktat.common.config.rules.getCommonConfiguration
import com.saveourtool.diktat.common.config.rules.kotlinVersion
import org.junit.jupiter.api.Test
import java.nio.file.Paths
import kotlin.io.path.inputStream

class ConfigReaderTest {
    @Test
    fun `testing json reading`() {
        val rulesConfigList: List<RulesConfig>? = RulesConfigReader()
            .read(Paths.get("src/test/resources/test-rules-config.yml").inputStream())
        requireNotNull(rulesConfigList)
        assert(rulesConfigList.any { it.name == "CLASS_NAME_INCORRECT" && it.enabled })
        assert(rulesConfigList.find { it.name == "CLASS_NAME_INCORRECT" }?.configuration == emptyMap<String, String>())
        assert(rulesConfigList.find { it.name == "DIKTAT_COMMON" }
            ?.configuration?.get("domainName") == "com.saveourtool.diktat")
    }

    @Test
    fun `testing kotlin version`() {
        val rulesConfigList: List<RulesConfig>? = RulesConfigReader()
            .read(Paths.get("src/test/resources/test-rules-config.yml").inputStream())
        requireNotNull(rulesConfigList)
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
