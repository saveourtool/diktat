package com.saveourtool.diktat.ruleset.chapter2

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.constants.Warnings.HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE
import com.saveourtool.diktat.ruleset.constants.Warnings.HEADER_MISSING_OR_WRONG_COPYRIGHT
import com.saveourtool.diktat.ruleset.constants.Warnings.HEADER_NOT_BEFORE_PACKAGE
import com.saveourtool.diktat.ruleset.constants.Warnings.HEADER_WRONG_FORMAT
import com.saveourtool.diktat.ruleset.rules.chapter2.comments.HeaderCommentRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

import java.time.LocalDate

class HeaderCommentRuleTest : LintTestBase(::HeaderCommentRule) {
    private val ruleId: String = "$DIKTAT_RULE_SET_ID:${HeaderCommentRule.NAME_ID}"
    private val curYear = LocalDate.now().year
    private val rulesConfigList: List<RulesConfig> = listOf(
        RulesConfig("HEADER_MISSING_OR_WRONG_COPYRIGHT", true,
            mapOf("copyrightText" to "Copyright (c) My Company, Ltd. 2012-$curYear. All rights reserved."))
    )
    private val rulesConfigListWithPattern: List<RulesConfig> = listOf(
        RulesConfig("HEADER_MISSING_OR_WRONG_COPYRIGHT", true,
            mapOf("copyrightText" to "Copyright (c) My Company, Ltd. 2012-;@currYear;. All rights reserved."))
    )
    private val rulesConfigListInvalidYear: List<RulesConfig> = listOf(
        RulesConfig("HEADER_MISSING_OR_WRONG_COPYRIGHT", true,
            mapOf("copyrightText" to "Copyright (c) My Company, Ltd. 2012-2019. All rights reserved."))
    )
    private val rulesConfigListInvalidYearBeforeCopyright: List<RulesConfig> = listOf(
        RulesConfig("HEADER_MISSING_OR_WRONG_COPYRIGHT", true,
            mapOf("copyrightText" to "Copyright (c) 2019 My Company, Ltd. All rights reserved."))
    )
    private val rulesConfigListYear: List<RulesConfig> = listOf(
        RulesConfig("HEADER_MISSING_OR_WRONG_COPYRIGHT", true,
            mapOf("copyrightText" to "Copyright (c) $curYear My Company, Ltd. All rights reserved."))
    )
    private val rulesConfigListYearWithPattern: List<RulesConfig> = listOf(
        RulesConfig("HEADER_MISSING_OR_WRONG_COPYRIGHT", true,
            mapOf("copyrightText" to "Copyright (c) ;@currYear; My Company, Ltd. All rights reserved."))
    )
    private val rulesConfigListCn: List<RulesConfig> = listOf(
        RulesConfig("HEADER_MISSING_OR_WRONG_COPYRIGHT", true,
            mapOf("copyrightText" to "版权所有 (c) 华为技术有限公司 2012-$curYear"))
    )
    private val curYearCopyright = "Copyright (c) My Company, Ltd. 2012-$curYear. All rights reserved."
    private val copyrightBlock = """
        /*
         * $curYearCopyright
         */
    """.trimIndent()

    @Test
    @Tag(WarningNames.HEADER_WRONG_FORMAT)
    fun `file header comment (positive example)`() {
        lintMethod(
            """
                $copyrightBlock
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package com.saveourtool.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.HEADER_WRONG_FORMAT)
    fun `file header comment with Chinese version copyright (positive example)`() {
        lintMethod(
            """
                /*
                 * 版权所有 (c) 华为技术有限公司 2012-$curYear
                 */
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package com.saveourtool.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
            rulesConfigList = rulesConfigListCn
        )
    }

    @Test
    @Tag(WarningNames.HEADER_MISSING_OR_WRONG_COPYRIGHT)
    fun `copyright should not be placed inside KDoc`() {
        lintMethod(
            """
                /**
                 * $curYearCopyright
                 */
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package com.saveourtool.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
            DiktatError(1, 1, ruleId, "${HEADER_MISSING_OR_WRONG_COPYRIGHT.warnText()} copyright is placed inside KDoc, but should be inside a block comment", true),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.HEADER_MISSING_OR_WRONG_COPYRIGHT)
    fun `copyright should not be placed inside KDoc (Chinese version)`() {
        lintMethod(
            """
                /**
                 * 版权所有 (c) 华为技术有限公司 2012-2020
                 */
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package com.saveourtool.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
            DiktatError(1, 1, ruleId, "${HEADER_MISSING_OR_WRONG_COPYRIGHT.warnText()} copyright is placed inside KDoc, but should be inside a block comment", true),
            rulesConfigList = rulesConfigListCn
        )
    }

    @Test
    @Tag(WarningNames.HEADER_MISSING_OR_WRONG_COPYRIGHT)
    fun `copyright should not be placed inside single line comment`() {
        lintMethod(
            """
                // $curYearCopyright
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package com.saveourtool.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
            DiktatError(1, 1, ruleId, "${HEADER_MISSING_OR_WRONG_COPYRIGHT.warnText()} copyright is placed inside KDoc, but should be inside a block comment", true),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.WRONG_COPYRIGHT_YEAR)
    fun `copyright year good`() {
        lintMethod(
            """
                /*
                 * $curYearCopyright
                 */
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package com.saveourtool.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.WRONG_COPYRIGHT_YEAR)
    fun `copyright year good 2`() {
        lintMethod(
            """
                /*
                 * Copyright (c) $curYear My Company, Ltd. All rights reserved.
                 */
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package com.saveourtool.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
            rulesConfigList = rulesConfigListYear
        )
    }

    @Test
    @Tag(WarningNames.WRONG_COPYRIGHT_YEAR)
    fun `copyright year good 3 - apply pattern`() {
        lintMethod(
            """
                /*
                 * Copyright (c) $curYear My Company, Ltd. All rights reserved.
                 */
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package com.saveourtool.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
            rulesConfigList = rulesConfigListYearWithPattern
        )
    }

    @Test
    @Tag(WarningNames.WRONG_COPYRIGHT_YEAR)
    fun `copyright year good 4 - apply pattern`() {
        lintMethod(
            """
                /*
                 * Copyright (c) My Company, Ltd. 2012-$curYear. All rights reserved.
                 */
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package com.saveourtool.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
            rulesConfigList = rulesConfigListWithPattern
        )
    }

    @Test
    @Tag(WarningNames.WRONG_COPYRIGHT_YEAR)
    fun `copyright year good 5`() {
        lintMethod(
            """
                /*
                   Copyright (c) My Company, Ltd. 2021-$curYear. All rights reserved.
                 */
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package com.saveourtool.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.WRONG_COPYRIGHT_YEAR)
    fun `copyright year good 6`() {
        lintMethod(
            """
                /*
                 *  Copyright (c) My Company, Ltd. 2002-$curYear. All rights reserved.
                 */
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package com.saveourtool.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.WRONG_COPYRIGHT_YEAR)
    fun `copyright year bad`() {
        lintMethod(
            """
                /*
                 * Copyright (c) My Company, Ltd. 2012-2019. All rights reserved.
                 */
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package com.saveourtool.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
            DiktatError(1, 1, ruleId, """${Warnings.WRONG_COPYRIGHT_YEAR.warnText()} year should be ${LocalDate.now().year}""", true),
            rulesConfigList = rulesConfigListInvalidYear
        )
    }

    @Test
    @Tag(WarningNames.WRONG_COPYRIGHT_YEAR)
    fun `copyright year bad 2`() {
        lintMethod(
            """
                /*
                 * Copyright (c) 2019 My Company, Ltd. All rights reserved.
                 */
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package com.saveourtool.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
            DiktatError(1, 1, ruleId, """${Warnings.WRONG_COPYRIGHT_YEAR.warnText()} year should be ${LocalDate.now().year}""", true),
            rulesConfigList = rulesConfigListInvalidYearBeforeCopyright
        )
    }

    @Test
    @Tag(WarningNames.HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE)
    fun `file with zero classes should have header KDoc`() {
        lintMethod(
            """
                package com.saveourtool.diktat.example

                val CONSTANT = 42

                fun foo(): Int = 42
            """.trimIndent(),
            DiktatError(1, 1, ruleId, "${HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE.warnText()} there are 0 declared classes and/or objects", false),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE)
    fun `file with multiple classes should have header KDoc`() {
        lintMethod(
            """
                package com.saveourtool.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
            DiktatError(1, 1, ruleId, "${HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE.warnText()} there are 2 declared classes and/or objects", false),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.HEADER_WRONG_FORMAT)
    fun `header KDoc should have newline after it`() {
        lintMethod(
            """
               |$copyrightBlock
               |/**
               | * Very useful description
               | * foo bar baz
               | */
               |package com.saveourtool.diktat.example
               |
               |class Example { }
            """.trimMargin(),
            DiktatError(4, 1, ruleId, "${HEADER_WRONG_FORMAT.warnText()} header KDoc should have a new line after", true),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.HEADER_NOT_BEFORE_PACKAGE)
    fun `header KDoc should be placed before package and imports`() {
        lintMethod(
            """
                |package com.saveourtool.diktat.example
                |
                |import com.saveourtool.diktat.example.Foo
                |
                |/**
                | * This is a code snippet for tests
                | */
                |
                |/**
                | * This is an example class
                | */
                |class Example { }
            """.trimMargin(),
            DiktatError(5, 1, ruleId, "${HEADER_NOT_BEFORE_PACKAGE.warnText()} header KDoc is located after package or imports", true),
            rulesConfigList = emptyList()
        )
    }

    @Test
    @Tag(WarningNames.HEADER_NOT_BEFORE_PACKAGE)
    fun `header KDoc object check`() {
        lintMethod(
            """
                |package com.saveourtool.diktat.example
                |
                |import com.saveourtool.diktat.example.Foo
                |
                |object TestEntry {
                |@JvmStatic
                |fun main(args: Array<String>) {
                |   val properties = TestFrameworkProperties("com/saveourtool/diktat/test/framework/test_framework.properties")
                |   TestProcessingFactory(TestArgumentsReader(args, properties, javaClass.classLoader)).processTests()
                |  }
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.HEADER_NOT_BEFORE_PACKAGE)
    fun `header KDoc object and class check`() {
        lintMethod(
            """
                |package com.saveourtool.diktat.example
                |
                |import com.saveourtool.diktat.example.Foo
                |
                |object TestEntry {
                |@JvmStatic
                |fun main(args: Array<String>) {
                |   val properties = TestFrameworkProperties("com/saveourtool/diktat/test/framework/test_framework.properties")
                |   TestProcessingFactory(TestArgumentsReader(args, properties, javaClass.classLoader)).processTests()
                |  }
                |}
                |
                |class Some {}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE.warnText()} there are 2 declared classes and/or objects"),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.HEADER_NOT_BEFORE_PACKAGE)
    fun `header KDoc in gradle script`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                |version = "0.1.0-SNAPSHOT"
                |
            """.trimMargin(),
            tempDir = tempDir,
            fileName = "src/main/kotlin/com/saveourtool/diktat/builds.gradle.kts"
        )
    }

    @Test
    @Tag(WarningNames.HEADER_NOT_BEFORE_PACKAGE)
    fun `header KDoc in kts script`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                |val version = "0.1.0-SNAPSHOT"
                |
            """.trimMargin(),
            tempDir = tempDir,
            fileName = "src/main/kotlin/com/saveourtool/diktat/Example.kts",
            DiktatError(1, 1, ruleId, "${HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE.warnText()} there are 0 declared classes and/or objects")
        )
    }
}
