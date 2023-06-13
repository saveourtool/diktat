package com.saveourtool.diktat.ruleset.smoke

import com.saveourtool.diktat.api.DiktatError
import com.saveourtool.diktat.ktlint.format
import com.saveourtool.diktat.ruleset.rules.DiktatRuleConfigReaderImpl
import com.saveourtool.diktat.ruleset.rules.DiktatRuleSetFactoryImpl
import com.saveourtool.diktat.test.framework.processing.TestComparatorUnit
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import java.nio.file.Path
import kotlin.io.path.inputStream

/**
 * Test for [DiktatRuleSetFactoryImpl] in autocorrect mode as a whole. All rules are applied to a file.
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
                ruleSetSupplier = { DiktatRuleSetFactoryImpl().invoke(DiktatRuleConfigReaderImpl().invoke(config.inputStream())) },
                file = testFile,
                cb = { lintError, _ -> unfixedLintErrors.add(lintError) },
            )
        },
    )
}
