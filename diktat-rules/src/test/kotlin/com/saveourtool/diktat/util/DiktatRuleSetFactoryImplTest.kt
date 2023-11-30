/**
 * Stub for diktat ruleset provide to be used in tests and other related utilities
 */

package com.saveourtool.diktat.util

import com.saveourtool.diktat.api.DiktatRuleSet
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.config.DiktatRuleConfigYamlReader
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.rules.DiktatRuleSetFactoryImpl
import com.saveourtool.diktat.test.framework.util.filterContentMatches

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.Path
import kotlin.io.path.isRegularFile
import kotlin.io.path.nameWithoutExtension
import kotlin.io.path.walk

class DiktatRuleSetFactoryImplTest {
    @OptIn(ExperimentalPathApi::class)
    @Suppress("UnsafeCallOnNullableType")
    @Test
    fun `check DiktatRuleSetFactoryImpl contain all rules`() {
        val path = "${System.getProperty("user.dir")}/src/main/kotlin/com/saveourtool/diktat/ruleset/rules"
        val fileNames = Path(path)
            .walk()
            .filter(Path::isRegularFile)
            .filterContentMatches(linesToRead = 150, Regex(""":\s*(?:Diktat)?Rule\s*\("""))
            .map(Path::nameWithoutExtension)
            .filterNot { it in ignoredFileNames }
            .toList()
        val ruleNames = DiktatRuleSetFactoryImpl()
            .invoke(emptyList())
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
            rulesConfigList ?: Companion::class.java.classLoader.getResourceAsStream("diktat-analysis.yml")
                ?.let { DiktatRuleConfigYamlReader().invoke(it) }
                .orEmpty()
        }
            .let(ruleSupplier)
            .let {
                DiktatRuleSet(listOf(it))
            }
    }
}
