package org.cqfn.diktat.ruleset.chapter2

import org.cqfn.diktat.ruleset.rules.kdoc.KdocMethods
import org.cqfn.diktat.util.FixTestBase
import org.junit.Test

class KdocMethodsFixTest : FixTestBase("test/paragraph2/kdoc/methods", KdocMethods()) {
    @Test
    fun `Rule should suggest KDoc template for missing KDocs`() {
        fixAndCompare("MissingKdocExpected.kt", "MissingKdocTest.kt")
    }

    @Test
    fun `KDoc template should be placed before modifiers`() {
        fixAndCompare("MissingKdocWithModifiersExpected.kt", "MissingKdocWithModifiersTest.kt")
    }

    @Test
    fun `Rule should not suggest empty KDoc templates`() {
        fixAndCompare("EmptyKdocExpected.kt", "EmptyKdocTest.kt")
    }

    @Test
    fun `@param tag should be added to existing KDoc`() {
        fixAndCompare("ParamTagInsertionExpected.kt", "ParamTagInsertionTest.kt")
    }

    @Test
    fun `@return tag should be added to existing KDoc`() {
        fixAndCompare("ReturnTagInsertionExpected.kt", "ReturnTagInsertionTest.kt")
    }

    @Test
    fun `@throws tag should be added to existing KDoc`() {
        fixAndCompare("ThrowsTagInsertionExpected.kt", "ThrowsTagInsertionTest.kt")
    }

    @Test
    fun `KdocMethods rule should reformat code (full example)`() {
        fixAndCompare("KdocMethodsFullExpected.kt", "KdocMethodsFullTest.kt")
    }
}
