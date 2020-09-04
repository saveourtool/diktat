package org.cqfn.diktat.ruleset.smoke

import org.cqfn.diktat.ruleset.rules.DiktatRuleSetProvider
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

// fixme: run as a separate maven goal/module?
class DiktatSmokeTest : FixTestBase("test/smoke", DiktatRuleSetProvider().get()) {
    @Test
    @Tag("DiktatRuleSetProvider")
    fun `smoke test #1`() {
        // todo add callback to collect unfixed errors
        fixAndCompare("Example1Expected.kt", "Example1Test.kt")
    }
}