package org.cqfn.diktat.util

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.io.File
import kotlin.reflect.jvm.reflect

/**
 * simple class for emulating RuleSetProvider to inject .yml rule configuration and mock this part of code
 */
class DiktatRuleSetProvider4Test(private val ruleSupplier: (rulesConfigList: List<RulesConfig>) -> Rule,
                                 rulesConfigList: List<RulesConfig>?) : RuleSetProvider {
    private val rulesConfigList: List<RulesConfig>? = rulesConfigList ?: RulesConfigReader(javaClass.classLoader).readResource("diktat-analysis.yml")

    override fun get(): RuleSet {
        return RuleSet(
                DIKTAT_RULE_SET_ID,
                ruleSupplier.invoke(rulesConfigList ?: emptyList())
        )
    }
}

class DiktatRuleSetProviderTest {

    companion object {
        private val IGNORE_FILE = listOf("DiktatRuleSetProvider")
    }

    @Suppress("UnsafeCallOnNullableType")
    @Test
    fun `check DiktatRuleSetProviderTest contain all rules`() {
        val path = "${System.getProperty("user.dir")}/src/main/kotlin/org/cqfn/diktat/ruleset/rules"
        val filesName = File(path).walk().filter { it.isFile }.map { it.nameWithoutExtension }.filterNot { it in IGNORE_FILE }
        val rulesName = DiktatRuleSetProvider().get().map { it::class.simpleName!! }
        Assertions.assertEquals(filesName.sorted().toList(), rulesName.sorted())
    }
}
