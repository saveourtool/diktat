package org.cqfn.diktat.ruleset.chapter2

import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_MISSING_OR_WRONG_COPYRIGHT
import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_WRONG_FORMAT
import org.cqfn.diktat.ruleset.rules.comments.HeaderCommentRule
import org.cqfn.diktat.util.FixTestBase
import org.cqfn.diktat.common.config.rules.RulesConfig
import generated.WarningNames
import generated.WarningNames.WRONG_COPYRIGHT_YEAR
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test

class HeaderCommentRuleFixTest : FixTestBase(
        "test/paragraph2/header",
        ::HeaderCommentRule,
        listOf(
                RulesConfig("HEADER_MISSING_OR_WRONG_COPYRIGHT", true,
                        mapOf(
                                "isCopyrightMandatory" to "true",
                                "copyrightText" to "Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.")
                ),
                RulesConfig("HEADER_WRONG_FORMAT", true, mapOf())
        )
) {
    @Test
    @Tag(WarningNames.HEADER_WRONG_FORMAT)
    fun `new line should be inserted after header KDoc`() {
        fixAndCompare("NewlineAfterHeaderKdocExpected.kt", "NewlineAfterHeaderKdocTest.kt")
    }

    @Test
    @Tag(WarningNames.HEADER_MISSING_OR_WRONG_COPYRIGHT)
    fun `if no copyright is present and mandatoryCopyright=true, it is added`() {
        fixAndCompare("AutoCopyrightExpected.kt", "AutoCopyrightTest.kt")
    }

    @Test
    @Tag(WarningNames.HEADER_NOT_BEFORE_PACKAGE)
    fun `header KDoc should be moved before package`() {
        fixAndCompare("MisplacedHeaderKdocExpected.kt", "MisplacedHeaderKdocTest.kt")
    }

    @Test
    @Tags(Tag(WarningNames.HEADER_MISSING_OR_WRONG_COPYRIGHT), Tag(WarningNames.HEADER_WRONG_FORMAT))
    fun `header KDoc should be moved before package - no copyright`() {
        fixAndCompare("MisplacedHeaderKdocNoCopyrightExpected.kt", "MisplacedHeaderKdocNoCopyrightTest.kt",
                listOf(RulesConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT.name, false, mapOf()), RulesConfig(HEADER_WRONG_FORMAT.name, true, mapOf()))
        )
    }

    @Test
    @Tags(Tag(WarningNames.HEADER_NOT_BEFORE_PACKAGE), Tag(WarningNames.HEADER_MISSING_OR_WRONG_COPYRIGHT))
    fun `header KDoc should be moved before package - appended copyright`() {
        fixAndCompare("MisplacedHeaderKdocAppendedCopyrightExpected.kt", "MisplacedHeaderKdocAppendedCopyrightTest.kt")
    }

    @Test
    @Tag(WRONG_COPYRIGHT_YEAR)
    fun `copyright invalid year should be auto-corrected`() {
        fixAndCompare("CopyrightDifferentYearExpected.kt", "CopyrightDifferentYearTest.kt",
                listOf(RulesConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT.name, true, mapOf(
                        "isCopyrightMandatory" to "true",
                        "copyrightText" to "Copyright (c) My Company., Ltd. 2012-2019. All rights reserved."
                )))
        )
    }
}
