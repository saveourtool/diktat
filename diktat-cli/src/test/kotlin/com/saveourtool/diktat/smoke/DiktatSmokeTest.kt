package com.saveourtool.diktat.smoke

import com.saveourtool.diktat.api.DiktatError
import com.saveourtool.diktat.ktlint.format
import com.saveourtool.diktat.ruleset.config.DiktatRuleConfigYamlReader
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
        val result = getTestComparatorUnit(config)
            .compareFilesFromResources(expected, test)
        result.assertSuccessful()
    }

    @BeforeEach
    internal fun setUp() {
        unfixedLintErrors.clear()
    }

    override fun assertUnfixedLintErrors(diktatErrorConsumer: (List<DiktatError>) -> Unit) {
        diktatErrorConsumer(unfixedLintErrors)
    }

    private fun getTestComparatorUnit(config: Path) = TestComparatorUnit(
        resourceReader = { tempDir.resolve("src/main/kotlin").resolve(it).normalize() },
        function = { testFile ->
            format(
                ruleSetSupplier = {
                    val diktatRuleConfigReader = DiktatRuleConfigYamlReader()
                    val diktatRuleSetFactory = DiktatRuleSetFactoryImpl()
                    diktatRuleSetFactory(diktatRuleConfigReader(config.inputStream()))
                },
                file = testFile,
                cb = { lintError, _ -> unfixedLintErrors.add(lintError) },
            )
        },
    )
}
