/**
 * Stub for diktat ruleset provide to be used in tests and other related utilities
 */

package com.saveourtool.diktat.util

import com.saveourtool.diktat.api.DiktatRuleSet
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.RulesConfigReader
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.rules.DiktatRuleSetProvider
import com.saveourtool.diktat.test.framework.util.filterContentMatches

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.walk

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
        val ruleNames = DiktatRuleSetProvider()
            .invoke()
            .rules
            .asSequence()
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

        /**
         * Simple method to emulate [DiktatRuleSet] to inject `.yml` rule configuration and mock this part of code.
         */
        internal fun diktatRuleSetForTest(
            ruleSupplier: (rulesConfigList: List<RulesConfig>) -> DiktatRule,
            rulesConfigList: List<RulesConfig>?,
        ): DiktatRuleSet = run {
            rulesConfigList ?: RulesConfigReader(Companion::class.java.classLoader)
                .readResource("diktat-analysis.yml")
                .orEmpty()
        }
            .let(ruleSupplier)
            .let {
                DiktatRuleSet(listOf(it))
            }
    }
}
