package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.rules.chapter6.RunInScript
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class RunInScriptFixTest : FixTestBase("test/chapter6/script", ::RunInScript) {
    @Test
    @Tag(WarningNames.RUN_IN_SCRIPT)
    fun `should wrap into run`() {
        fixAndCompare("SimpleRunInScriptExpected.kts", "SimpleRunInScriptTest.kts")
    }
}
