package com.saveourtool.diktat.ruleset.chapter2

import com.saveourtool.diktat.ruleset.rules.chapter2.kdoc.KdocMethods
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test

class KdocMethodsFixTest : FixTestBase("test/paragraph2/kdoc/package/src/main/kotlin/com/saveourtool/diktat/kdoc/methods",
    ::KdocMethods) {
    @Test
    @Tag(WarningNames.MISSING_KDOC_TOP_LEVEL)
    fun `Rule should suggest KDoc template for missing KDocs`() {
        fixAndCompare("MissingKdocExpected.kt", "MissingKdocTested.kt")
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_ON_FUNCTION)
    fun `KDoc template should be placed before modifiers`() {
        fixAndCompare("MissingKdocWithModifiersExpected.kt", "MissingKdocWithModifiersTest.kt")
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_ON_FUNCTION)
    fun `KDoc should be for function with single line body`() {
        fixAndCompare("MissingKdocOnFunctionExpected.kt", "MissingKdocOnFunctionTest.kt")
    }

    @Test
    @Tag(WarningNames.KDOC_EMPTY_KDOC)
    fun `Rule should not suggest empty KDoc templates`() {
        fixAndCompare("EmptyKdocExpected.kt", "EmptyKdocTested.kt")
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG)
    fun `@param tag should be added to existing KDoc`() {
        fixAndCompare("ParamTagInsertionExpected.kt", "ParamTagInsertionTested.kt")
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_RETURN_TAG)
    fun `@return tag should be added to existing KDoc`() {
        fixAndCompare("ReturnTagInsertionExpected.kt", "ReturnTagInsertionTested.kt")
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_THROWS_TAG)
    fun `@throws tag should be added to existing KDoc`() {
        fixAndCompare("ThrowsTagInsertionExpected.kt", "ThrowsTagInsertionTested.kt")
    }

    @Test
    @Tags(
        Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG),
        Tag(WarningNames.KDOC_WITHOUT_RETURN_TAG),
        Tag(WarningNames.KDOC_WITHOUT_THROWS_TAG)
    )
    fun `KdocMethods rule should reformat code (full example)`() {
        fixAndCompare("KdocMethodsFullExpected.kt", "KdocMethodsFullTested.kt")
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_THROWS_TAG)
    fun `Should add throws tag only for throw without catch`() {
        fixAndCompare("KdocWithoutThrowsTagExpected.kt", "KdocWithoutThrowsTagTested.kt")
    }
}
