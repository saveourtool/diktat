package org.cqfn.diktat.ruleset.chapter6

import generated.WarningNames
import org.cqfn.diktat.util.FixTestBase
import org.cqfn.diktat.ruleset.rules.UselessOverride
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class UselessOverrideFixTest : FixTestBase("test/paragraph6/useless-override", ::UselessOverride) {

    @Test
    @Tag(WarningNames.USELESS_OVERRIDE)
    fun `fix nested functions`() {
        fixAndCompare("UselessOverrideExpected.kt", "UselessOverrideTest.kt")
    }

    @Test
    @Tag(WarningNames.USELESS_OVERRIDE)
    fun `fix nestedd functions`() {
        fixAndCompare("SeveravalSyperTypesExpected.kt", "SeveravalSyperTypesTest.kt")
    }
}
