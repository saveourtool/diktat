package org.cqfn.diktat.ruleset.chapter6

import generated.WarningNames
import generated.WarningNames.CLASS_SHOULD_NOT_BE_ABSTRACT
import org.cqfn.diktat.util.FixTestBase
import org.cqfn.diktat.ruleset.rules.classes.AbstractClassesRule
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class AbstractClassesFixTest : FixTestBase("test/paragraph6/abstract_classes", ::AbstractClassesRule) {
    @Test
    @Tag(CLASS_SHOULD_NOT_BE_ABSTRACT)
    fun `fix abstract class`() {
        fixAndCompare("ShouldRemoveAbstractKeywordExpected.kt", "ShouldRemoveAbstractKeywordTest.kt")
    }
}