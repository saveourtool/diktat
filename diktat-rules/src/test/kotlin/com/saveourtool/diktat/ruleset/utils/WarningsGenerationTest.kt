package com.saveourtool.diktat.ruleset.utils

import com.saveourtool.diktat.ruleset.constants.Warnings
import org.junit.jupiter.api.Test

class WarningsGenerationTest {
    @Test
    fun `checking that warnings has all proper fields filled`() {
        Warnings.values().forEach { warn ->
            assert(warn.ruleId.split(".").size == 3)
        }
    }
}
