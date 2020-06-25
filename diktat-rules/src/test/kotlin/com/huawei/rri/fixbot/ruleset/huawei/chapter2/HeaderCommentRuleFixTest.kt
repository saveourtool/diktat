package com.huawei.rri.fixbot.ruleset.huawei.chapter2

import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.HEADER_MISSING_OR_WRONG_COPYRIGHT
import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.HEADER_WRONG_FORMAT
import com.huawei.rri.fixbot.ruleset.huawei.rules.comments.HeaderCommentRule
import com.huawei.rri.fixbot.ruleset.huawei.utils.FixTestBase
import config.rules.RulesConfig
import org.junit.Test

class HeaderCommentRuleFixTest : FixTestBase(
    "test/paragraph2/header",
    HeaderCommentRule(),
    listOf(
        RulesConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT.name, true,
            mapOf(
                "isCopyrightMandatory" to "true",
                "copyrightText" to "Copyright (c) Huawei Technologies Co., Ltd. 2020-2020. All rights reserved.")
        ),
        RulesConfig(HEADER_WRONG_FORMAT.name, true, mapOf())
    )
) {

    @Test
    fun `new line should be inserted after header KDoc`() {
        fixAndCompare("NewlineAfterHeaderKdocExpected.kt", "NewlineAfterHeaderKdocTest.kt")
    }

    @Test
    fun `if no copyright is present and mandatoryCopyright=true, it is added`() {
        fixAndCompare("AutoCopyrightExpected.kt", "AutoCopyrightTest.kt")
    }
}
