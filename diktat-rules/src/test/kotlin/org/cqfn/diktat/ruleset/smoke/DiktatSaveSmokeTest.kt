package org.cqfn.diktat.ruleset.smoke

import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import org.cqfn.diktat.util.FixTestBase
import com.pinterest.ktlint.core.LintError
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class DiktatSaveSmokeTest : FixTestBase("test/smoke/src/main/kotlin",
    { DiktatRuleSetProvider(configFilePath) },
    { lintError, _ -> unfixedLintErrors.add(lintError) },
) {
    @Test
    @Tag("DiktatRuleSetProvider")
    fun `save smoke test #1`() {
        saveSmokeTest("LocalVariableWithOffsetExpected.kt", "LocalVariableWithOffsetTest.kt")
    }

    companion object {
        private const val DEFAULT_CONFIG_PATH = "../diktat-analysis.yml"
        private val unfixedLintErrors: MutableList<LintError> = mutableListOf()

        // by default using same yml config as for diktat code style check, but allow to override
        private var configFilePath = DEFAULT_CONFIG_PATH
    }
}
