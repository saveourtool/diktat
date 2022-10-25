/**
 * Stub for diktat ruleset provide to be used in tests and other related utilities
 */
@file:Suppress(
    "Deprecation"
)

package org.cqfn.diktat.util

import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import org.cqfn.diktat.ruleset.rules.OrderedRuleSet.Companion.delegatee

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import java.io.File

/**
 * simple class for emulating RuleSetProvider to inject .yml rule configuration and mock this part of code
 */
class DiktatRuleSetProvider4Test(private val ruleSupplier: (rulesConfigList: List<RulesConfig>) -> Rule,
                                 rulesConfigList: List<RulesConfig>?) : RuleSetProvider {
    private val rulesConfigList: List<RulesConfig>? = rulesConfigList ?: RulesConfigReader(javaClass.classLoader).readResource("diktat-analysis.yml")

    override fun get() = RuleSet(
        DIKTAT_RULE_SET_ID,
        ruleSupplier.invoke(rulesConfigList ?: emptyList())
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
            .filter { file ->
                /*
                 * Include only those files which contain `Rule` or `DiktatRule`
                 * descendants (any of the 1st 150 lines contains a superclass
                 * constructor call).
                 */
                val constructorCall = Regex(""":\s*(?:Diktat)?Rule\s*\(""")
                file.bufferedReader().lineSequence().take(150)
                    .any { line ->
                        line.contains(constructorCall)
                    }
            }
            .map { it.nameWithoutExtension }
            .filterNot { it in ignoreFile }
        val rulesName = DiktatRuleSetProvider().get()
            .asSequence()
            .onEachIndexed { index, rule ->
                if (index != 0) {
                    Assertions.assertTrue(
                        rule.visitorModifiers.any { it is Rule.VisitorModifier.RunAfterRule },
                        "Rule ${rule.id} doesn't contain Rule.VisitorModifier.RunAfterRule"
                    )
                }
            }
            .map { it.delegatee() }
            .map { it::class.simpleName!! }
            .filterNot { it == "DummyWarning" }
            .toList()
        assertThat(rulesName.sorted()).containsExactlyElementsOf(filesName.sorted().toList())
    }

    companion object {
        private val ignoreFile = listOf(
            "DiktatRule",
            "OrderedRuleSet",
        )
    }
}
