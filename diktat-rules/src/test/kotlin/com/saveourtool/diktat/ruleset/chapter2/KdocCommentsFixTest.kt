package com.saveourtool.diktat.ruleset.chapter2

import com.saveourtool.diktat.ruleset.rules.chapter2.kdoc.KdocComments
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test

class KdocCommentsFixTest : FixTestBase("test/paragraph2/kdoc/", ::KdocComments) {
    @Test
    @Tag(WarningNames.COMMENTED_BY_KDOC)
    fun `check fix code block with kdoc comment`() {
        fixAndCompare("KdocBlockCommentExpected.kt", "KdocBlockCommentTest.kt")
    }

    @Test
    @Tags(Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY), Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT), Tag(WarningNames.MISSING_KDOC_TOP_LEVEL))
    fun `check fix without class kdoc`() {
        fixAndCompare("ConstructorCommentNoKDocExpected.kt", "ConstructorCommentNoKDocTest.kt")
    }

    @Test
    @Tags(Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY), Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT))
    fun `check fix with class kdoc`() {
        fixAndCompare("ConstructorCommentExpected.kt", "ConstructorCommentTest.kt")
    }

    @Test
    @Tags(Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY), Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT))
    fun `check fix with properties in class kdoc`() {
        fixAndCompare("ConstructorCommentPropertiesExpected.kt", "ConstructorCommentPropertiesTest.kt")
    }

    @Test
    @Tags(Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY), Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT))
    fun `should preserve newlines when moving comments from value parameters`() {
        fixAndCompare("ConstructorCommentNewlineExpected.kt", "ConstructorCommentNewlineTest.kt")
    }
}
