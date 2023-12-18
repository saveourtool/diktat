package com.saveourtool.diktat.util

import com.saveourtool.diktat.api.DiktatCallback
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ktlint.check
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.util.DiktatRuleSetFactoryImplTest.Companion.diktatRuleSetForTest
import com.saveourtool.diktat.api.DiktatError
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Base class for testing rules without fixing code.
 * @property ruleSupplier mapping of list of [RulesConfig] into a [DiktatRule]
 * @property rulesConfigList optional custom rules config
 */
open class LintTestBase(private val ruleSupplier: (rulesConfigList: List<RulesConfig>) -> DiktatRule,
                        private val rulesConfigList: List<RulesConfig>? = null) {
    /**
     * Perform linting of [code], collect errors and compare with [expectedLintErrors]
     *
     * @param code code to check
     * @param expectedLintErrors expected errors
     * @param rulesConfigList optional override for `this.rulesConfigList`
     * @see lintResult
     */
    fun lintMethod(@Language("kotlin") code: String,
                   vararg expectedLintErrors: DiktatError,
                   rulesConfigList: List<RulesConfig>? = null,
    ) = doAssert(
        actualLintErrors = lintResult(code, rulesConfigList),
        description = "lint result for \"$code\"",
        expectedLintErrors = expectedLintErrors
    )

    /**
     * Perform linting of [code] by creating a file in [tempDir] with [fileName], collect errors and compare with [expectedLintErrors]
     *
     * @param code code to check
     * @param tempDir a path to temporary folder
     * @param fileName relative path to file which needs to be checked
     * @param expectedLintErrors expected errors
     * @param rulesConfigList optional override for `this.rulesConfigList`
     * @see lintResult
     */
    fun lintMethodWithFile(
        @Language("kotlin") code: String,
        tempDir: Path,
        fileName: String,
        vararg expectedLintErrors: DiktatError,
        rulesConfigList: List<RulesConfig>? = null,
    ) {
        val file = tempDir.resolve(fileName).also {
            it.parent.createDirectories()
            it.writeText(code)
        }
        lintMethodWithFile(
            file = file,
            expectedLintErrors = expectedLintErrors,
            rulesConfigList = rulesConfigList,
        )
    }

    /**
     * Perform linting of [file], collect errors and compare with [expectedLintErrors]
     *
     * @param file a path to file to check
     * @param expectedLintErrors expected errors
     * @param rulesConfigList optional override for `this.rulesConfigList`
     * @see lintResult
     */
    fun lintMethodWithFile(
        file: Path,
        vararg expectedLintErrors: DiktatError,
        rulesConfigList: List<RulesConfig>? = null,
    ) = doAssert(
        actualLintErrors = lintResult(file, rulesConfigList),
        description = "lint result for \"${file.readText()}\"",
        expectedLintErrors = expectedLintErrors
    )

    private fun doAssert(
        actualLintErrors: List<DiktatError>,
        description: String,
        vararg expectedLintErrors: DiktatError,
    ) {
        when {
            expectedLintErrors.size == 1 && actualLintErrors.size == 1 -> {
                val actual = actualLintErrors[0]
                val expected = expectedLintErrors[0]

                assertThat(actual)
                    .describedAs(description)
                    .isEqualTo(expected)
                assertThat(actual.canBeAutoCorrected)
                    .describedAs("canBeAutoCorrected")
                    .isEqualTo(expected.canBeAutoCorrected)
            }

            else -> assertThat(actualLintErrors)
                .describedAs(description)
                .apply {
                    when {
                        expectedLintErrors.isEmpty() -> isEmpty()
                        else -> containsExactly(*expectedLintErrors)
                    }
                }
        }
    }

    /**
     * Lints the [file] and returns the errors collected, but (unlike
     * [lintMethodWithFile]) doesn't make any assertions.
     *
     * @param file the file to check.
     * @param rulesConfigList an optional override for `this.rulesConfigList`.
     * @return the list of lint errors.
     * @see lintMethodWithFile
     */
    private fun lintResult(
        file: Path,
        rulesConfigList: List<RulesConfig>? = null,
    ): List<DiktatError> {
        val lintErrors: MutableList<DiktatError> = mutableListOf()

        check(
            ruleSetSupplier = { rulesConfigList.toDiktatRuleSet() },
            file = file,
            cb = lintErrors.collector(),
        )

        return lintErrors
    }

    /**
     * Lints the [code] and returns the errors collected, but (unlike
     * [lintMethodWithFile]) doesn't make any assertions.
     *
     * @param code the code to check.
     * @param rulesConfigList an optional override for `this.rulesConfigList`.
     * @return the list of lint errors.
     * @see lintMethodWithFile
     */
    protected fun lintResult(
        @Language("kotlin") code: String,
        rulesConfigList: List<RulesConfig>? = null,
    ): List<DiktatError> {
        val lintErrors: MutableList<DiktatError> = mutableListOf()

        check(
            ruleSetSupplier = { rulesConfigList.toDiktatRuleSet() },
            text = code,
            cb = lintErrors.collector(),
        )

        return lintErrors
    }

    private fun List<RulesConfig>?.toDiktatRuleSet() = diktatRuleSetForTest(ruleSupplier, this ?: rulesConfigList)

    companion object {
        private fun MutableList<DiktatError>.collector(): DiktatCallback = DiktatCallback { error, _ ->
            this += error
        }
    }
}
