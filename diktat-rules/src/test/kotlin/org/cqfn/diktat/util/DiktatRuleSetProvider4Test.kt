/**
 * Stub for diktat ruleset provide to be used in tests and other related utilities
 */

package org.cqfn.diktat.util

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
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
            .map { it.nameWithoutExtension }
            .filterNot { it in ignoreFile }
        val rulesName = DiktatRuleSetProvider().get()
            .onEachIndexed { index, rule ->
                if (index != 0) {
                    Assertions.assertTrue(
                        rule.visitorModifiers.any { it is Rule.VisitorModifier.RunAfterRule },
                        "Rule ${rule.id} doesn't contain Rule.VisitorModifier.RunAfterRule"
                    )
                }
            }
            .map { (it as? DiktatRuleSetProvider.OrderedRule)?.rule ?: it }
            .map { it::class.simpleName!! }
            .filter { it != "DummyWarning" }
        Assertions.assertEquals(filesName.sorted().toList(), rulesName.sorted())
    }

    @Test
    fun `check OrderedRule with VisitorModifier RunAfterRule`() {
        val ruleWithRunAfterRule = object : Rule("invalid-rule", setOf(VisitorModifier.RunAfterRule("another-rule"))) {
            override fun visit(
                node: ASTNode,
                autoCorrect: Boolean,
                emit: EmitType
            ) {
                // do nothing
            }
        }
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            DiktatRuleSetProvider.OrderedRule(ruleWithRunAfterRule, null)
        }
    }

    @Test
    fun `check OrderedRule`() {
        val rule1 = object : Rule("rule-first") {
            override fun visit(
                node: ASTNode,
                autoCorrect: Boolean,
                emit: EmitType
            ) {
                // do nothing
            }
        }
        val rule2 = object : Rule("rule-second") {
            override fun visit(
                node: ASTNode,
                autoCorrect: Boolean,
                emit: EmitType
            ) {
                // do nothing
            }
        }

        val orderedRule1 = DiktatRuleSetProvider.OrderedRule(rule1, null)
        Assertions.assertTrue(orderedRule1.visitorModifiers.none { it is Rule.VisitorModifier.RunAfterRule },
            "Found Rule.VisitorModifier.RunAfterRule for case without prevRule")

        val orderedRule2 = DiktatRuleSetProvider.OrderedRule(rule2, rule1)
        val visitorModifiers2 = orderedRule2.visitorModifiers
            .filterIsInstance<Rule.VisitorModifier.RunAfterRule>()
        Assertions.assertEquals(1, visitorModifiers2.size,
            "Found invalid count of Rule.VisitorModifier.RunAfterRule for case with prevRule")
        Assertions.assertEquals(rule1.id, visitorModifiers2[0].ruleId,
            "Invalid ruleId in Rule.VisitorModifier.RunAfterRule")
    }

    companion object {
        private val ignoreFile = listOf("DiktatRuleSetProvider", "DiktatRule")
    }
}
