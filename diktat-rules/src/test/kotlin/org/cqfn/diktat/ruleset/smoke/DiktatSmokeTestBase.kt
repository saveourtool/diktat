@file:Suppress(
    "MISSING_KDOC_CLASS_ELEMENTS",
    "MISSING_KDOC_ON_FUNCTION",
    "BACKTICKS_PROHIBITED",
)

package org.cqfn.diktat.ruleset.smoke

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import org.cqfn.diktat.util.FixTestBase
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.pinterest.ktlint.core.LintError
import org.junit.jupiter.api.Tag
import kotlinx.serialization.builtins.ListSerializer

/**
 * Base class for smoke test classes
 */
abstract class DiktatSmokeTestBase : FixTestBase("test/smoke/src/main/kotlin",
    { DiktatRuleSetProvider(configFilePath) },
    { lintError, _ -> unfixedLintErrors.add(lintError) },
) {
    /**
     * Disable some of the rules.
     *
     * @param rulesToDisable
     * @param rulesToOverride
     */
    @Suppress("UnsafeCallOnNullableType")
    open fun overrideRulesConfig(rulesToDisable: List<Warnings>, rulesToOverride: RuleToConfig = emptyMap()) {
        val rulesConfig = RulesConfigReader(javaClass.classLoader).readResource(DEFAULT_CONFIG_PATH)!!
            .toMutableList()
            .also { rulesConfig ->
                rulesToDisable.forEach { warning ->
                    rulesConfig.removeIf { it.name == warning.name }
                    rulesConfig.add(RulesConfig(warning.name, enabled = false, configuration = emptyMap()))
                }
                rulesToOverride.forEach { (warning, configuration) ->
                    rulesConfig.removeIf { it.name == warning }
                    rulesConfig.add(RulesConfig(warning, enabled = true, configuration = configuration))
                }
            }
            .toList()
        kotlin.io.path.createTempFile()
            .toFile()
            .also {
                configFilePath = it.absolutePath
            }
            .writeText(
                Yaml(configuration = YamlConfiguration(strictMode = true))
                    .encodeToString(ListSerializer(RulesConfig.serializer()), rulesConfig)
            )
    }

    @Tag("DiktatRuleSetProvider")
    abstract fun `regression - should not fail if package is not set`()

    @Tag("DiktatRuleSetProvider")
    abstract fun `smoke test #8 - anonymous function`()

    @Tag("DiktatRuleSetProvider")
    abstract fun `smoke test #7`()

    @Tag("DiktatRuleSetProvider")
    abstract fun `smoke test #6`()

    @Tag("DiktatRuleSetProvider")
    abstract fun `smoke test #5`()

    @Tag("DiktatRuleSetProvider")
    abstract fun `smoke test #4`()

    @Tag("DiktatRuleSetProvider")
    abstract fun `smoke test #3`()

    @Tag("DiktatRuleSetProvider")
    abstract fun `regression - shouldn't throw exception in cases similar to #371`()

    @Tag("DiktatRuleSetProvider")
    abstract fun `smoke test #2`()

    @Tag("DiktatRuleSetProvider")
    abstract fun `smoke test #1`()

    @Tag("DiktatRuleSetProvider")
    abstract fun `smoke test with kts files #2`()

    @Tag("DiktatRuleSetProvider")
    abstract fun `smoke test with kts files with package name`()

    @Tag("DiktatRuleSetProvider")
    abstract fun `regression - should correctly handle tags with empty lines`()

    @Tag("DiktatRuleSetProvider")
    abstract fun `regression - FP of local variables rule`()

    @Tag("DiktatRuleSetProvider")
    abstract fun `fix can cause long line`()

    companion object {
        const val DEFAULT_CONFIG_PATH = "../diktat-analysis.yml"
        val unfixedLintErrors: MutableList<LintError> = mutableListOf()

        // by default using same yml config as for diktat code style check, but allow to override
        var configFilePath = DEFAULT_CONFIG_PATH
    }
}
