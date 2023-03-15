package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.rules.chapter6.classes.StatelessClassesRule
import org.cqfn.diktat.util.FixTestBase

import org.cqfn.diktat.ruleset.constants.WarningsNames.OBJECT_IS_PREFERRED
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class StatelessClassesRuleFixTest : FixTestBase("test/chapter6/stateless_classes", ::StatelessClassesRule) {
    @Test
    @Tag(OBJECT_IS_PREFERRED)
    fun `fix class to object keyword`() {
        fixAndCompare("StatelessClassExpected.kt", "StatelessClassTest.kt")
    }
}
