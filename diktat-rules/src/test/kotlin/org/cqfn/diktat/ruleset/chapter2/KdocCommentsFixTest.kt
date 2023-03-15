package org.cqfn.diktat.ruleset.chapter2

import org.cqfn.diktat.ruleset.rules.chapter2.kdoc.KdocComments
import org.cqfn.diktat.util.FixTestBase

import org.cqfn.diktat.ruleset.constants.WarningsNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class KdocCommentsFixTest : FixTestBase("test/paragraph2/kdoc/", ::KdocComments) {
    @Test
    @Tag(WarningNames.COMMENTED_BY_KDOC)
    fun `check fix code block with kdoc comment`() {
        fixAndCompare("KdocBlockCommentExpected.kt", "KdocBlockCommentTest.kt")
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `check fix with class kdoc`() {
        fixAndCompare("ConstructorCommentExpected.kt", "ConstructorCommentTest.kt")
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `check fix without class kdoc`() {
        fixAndCompare("ConstructorCommentNoKDocExpected.kt", "ConstructorCommentNoKDocTest.kt")
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `should preserve newlines when moving comments from value parameters`() {
        fixAndCompare("ConstructorCommentNewlineExpected.kt", "ConstructorCommentNewlineTest.kt")
    }
}
