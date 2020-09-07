package org.cqfn.diktat.util

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import org.cqfn.diktat.ruleset.rules.RuleSetDiktat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import java.io.File
import java.net.URI
import java.nio.file.Path
import java.nio.file.Paths

/**
 * simple class for emulating RuleSetProvider to inject .json rule configuration and mock this part of code
 */
class DiktatRuleSetProvider4Test(val rule: Rule, rulesConfigList: List<RulesConfig>?) : RuleSetProvider {
    private val rulesConfigList: List<RulesConfig>? = rulesConfigList
            ?: RulesConfigReader(javaClass.classLoader).readResource("rules-config.json")

    override fun get(): RuleSet {
        return RuleSetDiktat(
                rulesConfigList ?: listOf(),
                rule
        )
    }
}

class DiktatRuleSetProviderTest {

    companion object {
        private val IGNORE_FILE = listOf("DiktatRuleSetProvider")
    }

    @Test
    fun `check DiktatRuleSetProviderTest contain all rules`() {
        val path = "${System.getProperty("user.dir")}/src/main/kotlin/org/cqfn/diktat/ruleset/rules"
        val rulesName = File(path).walk().filter { it.isFile }.map { it.nameWithoutExtension }.toMutableList()
        rulesName.removeAll(IGNORE_FILE)
        val q = DiktatRuleSetProvider().get()
                .map { it.toString().split('.').last() }
                .map { it.substring(0, it.indexOf('@')) }
                .toMutableList()
        Assertions.assertEquals(rulesName.sort(), q.sort())
    }
}
