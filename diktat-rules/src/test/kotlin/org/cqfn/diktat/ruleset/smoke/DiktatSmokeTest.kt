package org.cqfn.diktat.ruleset.smoke

import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.YamlConfiguration
import com.pinterest.ktlint.core.LintError
import kotlinx.serialization.encodeToString
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.constants.Warnings.EMPTY_BLOCK_STRUCTURE_ERROR
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_NAME_INCORRECT
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_NAME_MATCH_CLASS
import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_EMPTY_TAGS
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_CLASS_ELEMENTS
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_ON_FUNCTION
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_TOP_LEVEL
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import org.cqfn.diktat.util.FixTestBase
import org.cqfn.diktat.util.assertEquals
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import java.io.File

/**
 * Test for [DiktatRuleSetProvider] in autocorrect mode as a whole. All rules are applied to a file.
 * Note: ktlint uses initial text from a file to calculate line and column from offset. Because of that line/col of unfixed errors
 * may change after some changes to text or other rules.
 *
 * fixme: run as a separate maven goal/module?
 */
class DiktatSmokeTest : FixTestBase("test/smoke/src/main/kotlin",
        { DiktatRuleSetProvider(configFilePath) },
        { lintError, _ -> unfixedLintErrors.add(lintError) },
        null
) {
    companion object {
        private const val DEFAULT_CONFIG_PATH = "../diktat-analysis.yml"
        private val unfixedLintErrors: MutableList<LintError> = mutableListOf()
        // by default using same yml config as for diktat code style check, but allow to override
        private var configFilePath = DEFAULT_CONFIG_PATH
    }

    @BeforeEach
    fun `prepare accumulator for LintErrors`() {
        unfixedLintErrors.clear()
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test #1`() {
        fixAndCompare("Example1Expected.kt", "Example1Test.kt")
        unfixedLintErrors.assertEquals(
                LintError(1, 1, "$DIKTAT_RULE_SET_ID:file-naming", "${FILE_NAME_INCORRECT.warnText()} Example1Test.kt_copy", true), // todo this is a false one
                LintError(1, 1, "$DIKTAT_RULE_SET_ID:file-naming", "${FILE_NAME_MATCH_CLASS.warnText()} Example1Test.kt_copy vs Example", true), // todo this is a false one
                LintError(3, 6, "$DIKTAT_RULE_SET_ID:kdoc-comments", "${MISSING_KDOC_TOP_LEVEL.warnText()} Example", false),
                LintError(3, 26, "$DIKTAT_RULE_SET_ID:kdoc-comments", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} isValid", false),
                LintError(6, 9, "$DIKTAT_RULE_SET_ID:kdoc-comments", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} foo", false),
                LintError(8, 8, "$DIKTAT_RULE_SET_ID:kdoc-comments", "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} foo", false),
                LintError(8, 8, "$DIKTAT_RULE_SET_ID:kdoc-methods", "${MISSING_KDOC_ON_FUNCTION.warnText()} foo", false),
                LintError(9, 3, "$DIKTAT_RULE_SET_ID:empty-block-structure", EMPTY_BLOCK_STRUCTURE_ERROR.warnText() +
                        " empty blocks are forbidden unless it is function with override keyword", false),
                LintError(12, 10, "$DIKTAT_RULE_SET_ID:kdoc-formatting", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
                LintError(14, 3, "$DIKTAT_RULE_SET_ID:kdoc-formatting", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false),
                LintError(19, 15, "$DIKTAT_RULE_SET_ID:kdoc-formatting", "${KDOC_NO_EMPTY_TAGS.warnText()} @return", false)
        )
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test #2`() {
        // fixme: path shouldn't point to `target` directory
        // val expectedFileAbsolutePath = Path.of(Path.of("target/test-classes/$resourceFilePath").toAbsolutePath().toString(), "Example2Test.kt_copy")

        val expectedFileAbsolutePath = File("target/test-classes/$resourceFilePath/Example2Test.kt_copy").absolutePath
        fixAndCompare("Example2Expected.kt", "Example2Test.kt")
        unfixedLintErrors.assertEquals(
                LintError(1, 1, "$DIKTAT_RULE_SET_ID:file-naming", "${FILE_NAME_INCORRECT.warnText()} Example2Test.kt_copy", true), // todo this is a false one
                LintError(1, 1, "$DIKTAT_RULE_SET_ID:header-comment", "${HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE.warnText()} $expectedFileAbsolutePath", false)
        )
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `regression - shouldn't throw exception in cases similar to #371`() {
        fixAndCompare("Bug1Expected.kt", "Bug1Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test #3`() {
        fixAndCompare("Example3Expected.kt", "Example3Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test #4`() {
        fixAndCompare("Example4Expected.kt", "Example4Test.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `regression - should not fail if package is not set`() {
        overrideRulesConfig(listOf(Warnings.PACKAGE_NAME_MISSING, Warnings.PACKAGE_NAME_INCORRECT_PATH,
            Warnings.PACKAGE_NAME_INCORRECT_PREFIX))
        fixAndCompare("DefaultPackageExpected.kt", "DefaultPackageTest.kt")
    }

    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test with multiplatform project layout`() {
        fixAndCompare("../../jsMain/kotlin/org/cqfn/diktat/scripts/ScriptExpected.kt",
        "../../jsMain/kotlin/org/cqfn/diktat/scripts/ScriptTest.kt")
    }

    @AfterEach
    fun `revert configuration file to default`() {
        configFilePath = DEFAULT_CONFIG_PATH
    }

    /**
     * Disable some of the rules.
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun overrideRulesConfig(rulesToDisable: List<Warnings>) {
        val rulesConfig = RulesConfigReader(javaClass.classLoader).readResource(configFilePath)!!
            .toMutableList()
            .also { rulesConfig ->
                rulesToDisable.forEach { warning ->
                    rulesConfig.removeIf { it.name ==  warning.name }
                    rulesConfig.add(RulesConfig(warning.name, enabled = false, configuration = emptyMap()))
                }
            }
        createTempFile()
            .also {
                configFilePath = it.absolutePath
            }
            .writeText(
                Yaml(configuration = YamlConfiguration(strictMode = true))
                    .encodeToString(rulesConfig)
            )
    }
}
