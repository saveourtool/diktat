package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.ruleset.rules.chapter3.PreviewAnnotationRule
import com.saveourtool.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class PreviewAnnotationFixTest : FixTestBase("test/paragraph3/preview_annotation", ::PreviewAnnotationRule) {
    @Test
    @Tag(WarningNames.PREVIEW_ANNOTATION)
    fun `should add private modifier`() {
        fixAndCompare("PreviewAnnotationPrivateModifierExpected.kt", "PreviewAnnotationPrivateModifierTest.kt")
    }

    @Test
    @Tag(WarningNames.PREVIEW_ANNOTATION)
    fun `should add Preview suffix`() {
        fixAndCompare("PreviewAnnotationMethodNameExpected.kt", "PreviewAnnotationMethodNameTest.kt")
    }
}
