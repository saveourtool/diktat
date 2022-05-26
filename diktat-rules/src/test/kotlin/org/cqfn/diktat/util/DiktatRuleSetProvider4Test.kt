/**
 * Stub for diktat ruleset provide to be used in tests and other related utilities
 */

package org.cqfn.diktat.util

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import java.io.File

/**
 * simple class for emulating RuleSetProvider to inject .yml rule configuration and mock this part of code
 */
class DiktatRuleSetProvider4Test(private val ruleSupplier: (rulesConfigList: List<RulesConfig>, prevId: String?) -> Rule,
                                 rulesConfigList: List<RulesConfig>?) : RuleSetProvider {
    private val rulesConfigList: List<RulesConfig>? = rulesConfigList ?: RulesConfigReader(javaClass.classLoader).readResource("diktat-analysis.yml")

    override fun get() = RuleSet(
        DIKTAT_RULE_SET_ID,
        ruleSupplier.invoke(rulesConfigList ?: emptyList(), null)
    )
}

class DiktatRuleSetProviderTest {
    @Suppress("UnsafeCallOnNullableType")
    @Test
    fun `check DiktatRuleSetProviderTest contain all rules`() {
        val path = "${System.getProperty("user.dir")}/src/main/kotlin/org/cqfn/diktat/ruleset/rules"
        val filesName = File(path)
            .walk()
            .filter { it.isFile }
            .map { it.nameWithoutExtension }
            .filterNot { it in ignoreFile }
        val rulesName = DiktatRuleSetProvider().get().map { it::class.simpleName!! }.filter { it != "DummyWarning" }
        Assertions.assertEquals(filesName.sorted().toList(), rulesName.sorted())
    }

    companion object {
        private val ignoreFile = listOf("DiktatRuleSetProvider", "DiktatRule")
    }
}
