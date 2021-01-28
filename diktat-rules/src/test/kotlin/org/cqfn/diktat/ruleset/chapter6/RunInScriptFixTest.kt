package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Test
import org.cqfn.diktat.ruleset.rules.chapter6.RunInScript


class RunInScriptFixTest : FixTestBase("test/chapter6/script", ::RunInScript) {

    @Test
    fun `should wrap into run`() {
        fixAndCompare("SimpleRunInScriptExpected.kts", "SimpleRunInScriptTest.kts")
    }
}