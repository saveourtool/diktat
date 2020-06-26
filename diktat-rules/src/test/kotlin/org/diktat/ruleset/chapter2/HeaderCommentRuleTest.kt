package org.diktat.ruleset.chapter2

import org.diktat.ruleset.constants.Warnings.HEADER_CONTAINS_DATE_OR_AUTHOR
import org.diktat.ruleset.constants.Warnings.HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE
import org.diktat.ruleset.constants.Warnings.HEADER_MISSING_OR_WRONG_COPYRIGHT
import org.diktat.ruleset.constants.Warnings.HEADER_WRONG_FORMAT
import org.diktat.ruleset.rules.comments.HeaderCommentRule
import org.diktat.ruleset.utils.lintMethod
import com.pinterest.ktlint.core.LintError
import org.diktat.common.config.rules.RulesConfig
import org.junit.Test

class HeaderCommentRuleTest {
    private val rulesConfigList: List<RulesConfig> = listOf(
            RulesConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT.name, true,
                    mapOf("copyrightText" to "Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved."))
    )

    private val rulesConfigListCn: List<RulesConfig> = listOf(
            RulesConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT.name, true,
                    mapOf("copyrightText" to "版权所有 (c) 华为技术有限公司 2012-2020"))
    )

    private val copyrightBlock = """
        /*
         * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
         */
    """.trimIndent()

    @Test
    fun `file header comment (positive example)`() {
        val validCode = """
            $copyrightBlock
            /**
             * Very useful description, why this file has two classes
             * foo bar baz
             */

            package org.diktat.example

            class Example1 { }

            class Example2 { }
        """.trimIndent()

        lintMethod(HeaderCommentRule(), validCode, rulesConfigList = rulesConfigList)
    }

    @Test
    fun `file header comment with Chinese version copyright (positive example)`() {
        val validCode = """
            /*
             * 版权所有 (c) 华为技术有限公司 2012-2020
             */
            /**
             * Very useful description, why this file has two classes
             * foo bar baz
             */

            package org.diktat.example

            class Example1 { }

            class Example2 { }
        """.trimIndent()

        lintMethod(HeaderCommentRule(), validCode, rulesConfigList = rulesConfigListCn)
    }

    @Test
    fun `copyright should not be placed inside KDoc`() {
        val invalidCode = """
            /**
             * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
             */
            /**
             * Very useful description, why this file has two classes
             * foo bar baz
             */

            package org.diktat.example

            class Example1 { }

            class Example2 { }
        """.trimIndent()

        lintMethod(HeaderCommentRule(), invalidCode, LintError(1, 1, "header-comment",
            "${HEADER_MISSING_OR_WRONG_COPYRIGHT.warnText()} ", false),
            rulesConfigList = rulesConfigList)
    }

    @Test
    fun `copyright should not be placed inside KDoc (Chinese version)`() {
        val invalidCode = """
            /**
             * 版权所有 (c) 华为技术有限公司 2012-2020
             */
            /**
             * Very useful description, why this file has two classes
             * foo bar baz
             */

            package org.diktat.example

            class Example1 { }

            class Example2 { }
        """.trimIndent()

        lintMethod(HeaderCommentRule(), invalidCode, LintError(1, 1, "header-comment",
            "${HEADER_MISSING_OR_WRONG_COPYRIGHT.warnText()} ", false),
            rulesConfigList = rulesConfigListCn)
    }

    @Test
    fun `copyright should not be placed inside single line comment`() {
        val invalidCode = """
            // Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
            /**
             * Very useful description, why this file has two classes
             * foo bar baz
             */

            package org.diktat.example

            class Example1 { }

            class Example2 { }
        """.trimIndent()

        lintMethod(HeaderCommentRule(), invalidCode, LintError(1, 1, "header-comment",
            "${HEADER_MISSING_OR_WRONG_COPYRIGHT.warnText()} ", false),
        rulesConfigList = rulesConfigList)
    }

    @Test
    fun `@author tag is not allowed in header comment`() {
        val invalidCode = """
            $copyrightBlock
            /**
             * Description of this file
             * @author anonymous
             */

             package org.diktat.example

             class Example { }
        """.trimIndent()

        lintMethod(HeaderCommentRule(), invalidCode, LintError(4, 12, "header-comment",
            "${HEADER_CONTAINS_DATE_OR_AUTHOR.warnText()} * @author anonymous"),
            rulesConfigList = rulesConfigList)
    }

    @Test
    fun `file with zero classes should have header KDoc`() {
        val invalidCode = """
            package org.diktat.example

            val CONSTANT = 42

            fun foo(): Int = 42
        """.trimIndent()

        lintMethod(HeaderCommentRule(), invalidCode, LintError(1, 1, "header-comment",
            "${HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE.warnText()} ", false),
            rulesConfigList = rulesConfigList)
    }

    @Test
    fun `file with multiple classes should have header KDoc`() {
        val invalidCode = """
            package org.diktat.example

            class Example1 { }

            class Example2 { }
        """.trimIndent()

        lintMethod(HeaderCommentRule(), invalidCode, LintError(1, 1, "header-comment",
            "${HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE.warnText()} ", false),
            rulesConfigList = rulesConfigList)
    }

    @Test
    fun `header KDoc should have newline after it`() {
        val invalidCode = """
            $copyrightBlock
            /**
             * Very useful description
             * foo bar baz
             */
            package org.diktat.example

            class Example { }
        """.trimIndent()

        lintMethod(HeaderCommentRule(), invalidCode, LintError(4, 12, "header-comment",
            "${HEADER_WRONG_FORMAT.warnText()} header KDoc should have a new line after", true),
            rulesConfigList = rulesConfigList)
    }
}
