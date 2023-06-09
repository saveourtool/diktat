package org.cqfn.diktat.ruleset.smoke

import org.cqfn.diktat.api.DiktatError
import org.cqfn.diktat.ktlint.format
import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import org.cqfn.diktat.test.framework.processing.TestComparatorUnit
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import java.nio.file.Path
import kotlin.io.path.absolutePathString

/**
 * Test for [DiktatRuleSetProvider] in autocorrect mode as a whole. All rules are applied to a file.
 * Note: ktlint uses initial text from a file to calculate line and column from offset. Because of that line/col of unfixed errors
 * may change after some changes to text or other rules.
 */
class DiktatSmokeTest : DiktatSmokeTestBase() {
    private val unfixedLintErrors: MutableList<DiktatError> = mutableListOf()

    override fun fixAndCompare(
        config: Path,
        expected: String,
        test: String,
    ) {
        Assertions.assertTrue(
            getTestComparatorUnit(config)
                .compareFilesFromResources(expected, test)
                .isSuccessful
        )
    }

    @BeforeEach
    internal fun setUp() {
        unfixedLintErrors.clear()
    }

    override fun assertUnfixedLintErrors(diktatErrorConsumer: (List<DiktatError>) -> Unit) {
        diktatErrorConsumer(unfixedLintErrors)
    }

    private fun getTestComparatorUnit(config: Path) = TestComparatorUnit(
        resourceFilePath = RESOURCE_FILE_PATH,
        function = { testFile ->
            format(
                ruleSetSupplier = { DiktatRuleSetProvider(config.absolutePathString()).invoke() },
                file = testFile,
                cb = { lintError, _ -> unfixedLintErrors.add(lintError) },
            )
        },
    )
}
