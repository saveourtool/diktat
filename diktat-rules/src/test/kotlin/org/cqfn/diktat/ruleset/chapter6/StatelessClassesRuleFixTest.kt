package org.cqfn.diktat.ruleset.chapter6

import generated.WarningNames
import generated.WarningNames.OBJECT_IS_PREFERRED
import org.cqfn.diktat.util.FixTestBase
import org.cqfn.diktat.ruleset.rules.classes.StatelessClassesRule
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class StatelessClassesRuleFixTest: FixTestBase("test/chapter6/stateless_classes", ::StatelessClassesRule) {
    @Test
    @Tag(OBJECT_IS_PREFERRED)
    fun `test`() {
        fixAndCompare("StatelessClassExpected.kt", "StatelessClassTest.kt")
    }

}