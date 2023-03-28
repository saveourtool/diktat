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
import org.cqfn.diktat.ktlint.KtLintRuleSetWrapper
import org.cqfn.diktat.ktlint.KtLintRuleSetWrapper.Companion.delegatee
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.rules.DiktatRuleSet
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import org.cqfn.diktat.test.framework.util.filterContentMatches

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.walk

/**
 * simple class for emulating RuleSetProvider to inject .yml rule configuration and mock this part of code
 */
class DiktatRuleSetProvider4Test(private val ruleSupplier: (rulesConfigList: List<RulesConfig>) -> DiktatRule,
                                 rulesConfigList: List<RulesConfig>?) : RuleSetProvider {
    private val rulesConfigList: List<RulesConfig>? = rulesConfigList ?: RulesConfigReader(javaClass.classLoader).readResource("diktat-analysis.yml")

    @Suppress("OVERRIDE_DEPRECATION")
    override fun get(): RuleSet = KtLintRuleSetWrapper(DiktatRuleSet(listOf(ruleSupplier.invoke(rulesConfigList ?: emptyList()))))
}

class DiktatRuleSetProviderTest {
    @OptIn(ExperimentalPathApi::class)
    @Suppress("UnsafeCallOnNullableType")
    @Test
    fun `check DiktatRuleSetProviderTest contain all rules`() {
        val path = "${System.getProperty("user.dir")}/src/main/kotlin/org/cqfn/diktat/ruleset/rules"
        val fileNames = Path(path)
            .walk()
            .filter(Path::isRegularFile)
            .filterContentMatches(linesToRead = 150, Regex(""":\s*(?:Diktat)?Rule\s*\("""))
            .map(Path::nameWithoutExtension)
            .filterNot { it in ignoredFileNames }
            .toList()
        val ruleNames = DiktatRuleSetProvider().get()
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
            .map { it::class.simpleName }
            .filterNotNull()
            .filterNot { it in ignoredRuleNames }
            .toList()
        assertThat(fileNames).isNotEmpty
        assertThat(ruleNames).isNotEmpty
        assertThat(ruleNames.sorted()).containsExactlyElementsOf(fileNames.sorted())
    }

    companion object {
        private val ignoredFileNames = listOf(
            "DiktatRule",
            "OrderedRuleSet",
        )
        private val ignoredRuleNames = listOf(
            "DummyWarning",
        )
    }
}
