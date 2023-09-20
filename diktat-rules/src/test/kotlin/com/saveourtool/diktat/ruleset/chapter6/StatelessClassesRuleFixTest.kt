package com.saveourtool.diktat.ruleset.chapter6

import com.saveourtool.diktat.ruleset.rules.chapter6.classes.StatelessClassesRule
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames.OBJECT_IS_PREFERRED
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class StatelessClassesRuleFixTest : FixTestBase("test/chapter6/stateless_classes", ::StatelessClassesRule) {
    @Test
    @Tag(OBJECT_IS_PREFERRED)
    @Disabled("https://github.com/saveourtool/diktat/issues/1737")
    fun `fix class to object keyword`() {
        fixAndCompare("StatelessClassExpected.kt", "StatelessClassTest.kt")
    }
}
