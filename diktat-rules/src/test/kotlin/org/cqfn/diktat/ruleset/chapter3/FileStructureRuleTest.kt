package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_CONTAINS_ONLY_COMMENTS
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_INCORRECT_BLOCKS_ORDER
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_NO_BLANK_LINE_BETWEEN_BLOCKS
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_UNORDERED_IMPORTS
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_WILDCARD_IMPORTS
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter3.files.FileStructureRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class FileStructureRuleTest : LintTestBase(::FileStructureRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${FileStructureRule.NAME_ID}"
    private val rulesConfigListWildCardImport: List<RulesConfig> = listOf(
        RulesConfig(FILE_WILDCARD_IMPORTS.name, true,
            mapOf("allowedWildcards" to "org.cqfn.diktat.*"))
    )
    private val rulesConfigListWildCardImports: List<RulesConfig> = listOf(
        RulesConfig(FILE_WILDCARD_IMPORTS.name, true,
            mapOf("allowedWildcards" to "org.cqfn.diktat.*, org.cqfn.diktat.ruleset.constants.Warnings.*"))
    )
    private val rulesConfigListEmptyDomainName: List<RulesConfig> = listOf(
        RulesConfig("DIKTAT_COMMON", true, mapOf("domainName" to ""))
    )

    @Test
    @Tag(WarningNames.FILE_CONTAINS_ONLY_COMMENTS)
    fun `should warn if file contains only comments`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |/**
                | * This file appears to be empty
                | */
                |
                |
                |// lorem ipsum
            """.trimMargin(),
            LintError(1, 1, ruleId, "${FILE_CONTAINS_ONLY_COMMENTS.warnText()} file contains no code", false)
        )
    }

    @Test
    @Tag(WarningNames.FILE_INCORRECT_BLOCKS_ORDER)
    fun `should warn if file annotations are not immediately before package directive`() {
        lintMethod(
            """
                |@file:JvmName("Foo")
                |
                |/**
                | * This is an example file
                | */
                |
                |package org.cqfn.diktat.example
                |
                |class Example { }
            """.trimMargin(),
            LintError(1, 1, ruleId, "${FILE_INCORRECT_BLOCKS_ORDER.warnText()} @file:JvmName(\"Foo\")", true),
            LintError(3, 1, ruleId, "${FILE_INCORRECT_BLOCKS_ORDER.warnText()} /**", true)
        )
    }

    @Test
    @Tag(WarningNames.FILE_UNORDERED_IMPORTS)
    fun `should warn if imports are not sorted alphabetically`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |import org.junit.Test
                |import org.cqfn.diktat.Foo
                |
                |class Example { 
                |val x: Test = null
                |val y: Foo = null
                |}
            """.trimMargin(),
            LintError(3, 1, ruleId, "${FILE_UNORDERED_IMPORTS.warnText()} import org.cqfn.diktat.Foo...", true)
        )
    }

    @Test
    @Tag(WarningNames.FILE_WILDCARD_IMPORTS)
    fun `should warn if wildcard imports are used`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |import org.cqfn.diktat.*
                |
                |class Example { }
            """.trimMargin(),
            LintError(3, 1, ruleId, "${FILE_WILDCARD_IMPORTS.warnText()} import org.cqfn.diktat.*", false),
        )
    }

    @Test
    @Tag(WarningNames.FILE_NO_BLANK_LINE_BETWEEN_BLOCKS)
    fun `should warn if blank lines are wrong between code blocks`() {
        lintMethod(
            """
                |/**
                | * This is an example
                | */
                |@file:JvmName("Foo")
                |
                |
                |package org.cqfn.diktat.example
                |import org.cqfn.diktat.Foo
                |class Example{
                |val x: Foo = null
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${FILE_NO_BLANK_LINE_BETWEEN_BLOCKS.warnText()} /**", true),
            LintError(4, 1, ruleId, "${FILE_NO_BLANK_LINE_BETWEEN_BLOCKS.warnText()} @file:JvmName(\"Foo\")", true),
            LintError(7, 1, ruleId, "${FILE_NO_BLANK_LINE_BETWEEN_BLOCKS.warnText()} package org.cqfn.diktat.example", true),
            LintError(8, 1, ruleId, "${FILE_NO_BLANK_LINE_BETWEEN_BLOCKS.warnText()} import org.cqfn.diktat.Foo", true)
        )
    }

    @Test
    @Tag(WarningNames.FILE_WILDCARD_IMPORTS)
    fun `wildcard imports are used but with config`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |import org.cqfn.diktat.*
                |
                |class Example { }
            """.trimMargin(), rulesConfigList = rulesConfigListWildCardImport
        )
    }

    @Test
    @Tag(WarningNames.FILE_WILDCARD_IMPORTS)
    fun `wildcard imports are used but with several imports in config`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |import org.cqfn.diktat.*
                |import org.cqfn.diktat.ruleset.constants.Warnings.*
                |
                |class Example { }
            """.trimMargin(), rulesConfigList = rulesConfigListWildCardImports
        )
    }

    @Test
    @Tag(WarningNames.FILE_INCORRECT_BLOCKS_ORDER)
    fun `should warn if there are other misplaced comments before package - positive example`() {
        lintMethod(
            """
                |/**
                | * This is an example
                | */
                |
                |// some notes on this file
                |package org.cqfn.diktat.example
                |
                |import org.cqfn.diktat.Foo
                |
                |class Example{
                |val x: Foo = null
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.FILE_INCORRECT_BLOCKS_ORDER)
    fun `should warn if there are other misplaced comments before package`() {
        lintMethod(
            """
                |// some notes on this file
                |/**
                | * This is an example
                | */
                |
                |package org.cqfn.diktat.example
                |
                |import org.cqfn.diktat.Foo
                |
                |class Example{
                |val x: Foo = null
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${FILE_INCORRECT_BLOCKS_ORDER.warnText()} // some notes on this file", true),
            LintError(2, 1, ruleId, "${FILE_INCORRECT_BLOCKS_ORDER.warnText()} /**", true),
        )
    }

    @Test
    @Tag(WarningNames.FILE_INCORRECT_BLOCKS_ORDER)
    fun `block comment should be detected as copyright - positive example`() {
        lintMethod(
            """
                |/*
                | * Copyright Example Inc. (c)
                | */
                |
                |@file:Annotation
                |
                |package org.cqfn.diktat.example
                |
                |import org.cqfn.diktat.Foo
                |
                |class Example{
                |val x: Foo = null
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.FILE_INCORRECT_BLOCKS_ORDER)
    fun `block comment shouldn't be detected as copyright without keywords`() {
        lintMethod(
            """
                |/*
                | * Just a regular block comment
                | */
                |@file:Annotation
                |
                |package org.cqfn.diktat.example
                |
                |import org.cqfn.diktat.Foo
                |
                |class Example{
                |val x: Foo = null
                |}
            """.trimMargin(),
            LintError(4, 1, ruleId, "${FILE_INCORRECT_BLOCKS_ORDER.warnText()} @file:Annotation", true)
        )
    }

    @Test
    @Tag(WarningNames.FILE_INCORRECT_BLOCKS_ORDER)
    fun `test with empty domain name in config`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |import org.cqfn.diktat.Foo
                |import com.pinterest.ktlint.core.LintError
                |
                |class Example{
                |val x: LintError = null
                |val x: Foo = null
                |}
            """.trimMargin(),
            LintError(3, 1, ruleId, "${FILE_UNORDERED_IMPORTS.warnText()} import com.pinterest.ktlint.core.LintError...", true),
            rulesConfigList = rulesConfigListEmptyDomainName
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `import from the package`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |import org.cqfn.diktat.example.Foo
                |
                |class Example { 
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${Warnings.UNUSED_IMPORT.warnText()} org.cqfn.diktat.example.Foo - unused import", true)
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `unused import`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |import org.cqfn.diktat.Foo
                |
                |class Example { 
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${Warnings.UNUSED_IMPORT.warnText()} org.cqfn.diktat.Foo - unused import", true)
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `used import`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |import org.cqfn.diktat.Foo
                |
                |class Example { 
                |val x: Foo = null
                |}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `Operator overloading`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |import kotlin.io.path.div
                |
                |class Example { 
                |val pom = kotlin.io.path.createTempFile().toFile()
                |val x = listOf(pom.parentFile.toPath() / "src/main/kotlin/exclusion")
                |}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `Should correctly check infix functions`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |import org.cqfn.diktat.utils.logAndExit
                |
                |fun main() {
                |"Type is not supported yet" logAndExit 1
                |}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `unused import to infix functions`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |import org.cqfn.diktat.utils.logAndExit
                |
                |fun main() {
                |println("Type is not supported yet")
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${Warnings.UNUSED_IMPORT.warnText()} org.cqfn.diktat.utils.logAndExit - unused import", true)
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `Acute import`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |import js.externals.jquery.`${'$'}`
                |
                |fun main() {
                |   `${'$'}`("document").ready {}
                |}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `Ignore Imports`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |import com.example.get
                |import com.example.invoke
                |import com.example.set
                |
                |fun main() {
                |   val a = list[1]
                |}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `check by #1`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |import com.example.get
                |import com.example.invoke
                |import com.example.set
                |import kotlin.properties.Delegates
                |
                |fun main() {
                |   val a by Delegates.observable()
                |}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `check by #2 should not trigger`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |import com.example.equals
                |import com.example.get
                |import com.example.invoke
                |import com.example.set
                |import org.gradle.kotlin.dsl.provideDelegate
                |import tasks.getValue
                |import tasks.setValue
                |
                |fun main() {
                |   val a by tasks.getting  // `getValue` is used
                |   val b by project.tasks  // `provideDelegate` is used
                |   a != 0
                |}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `check by #3 should trigger`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |import Delegate
                |import com.example.equals
                |import com.example.get
                |import com.example.invoke
                |import com.example.set
                |
                |fun main() {
                |   val a: Foo
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${Warnings.UNUSED_IMPORT.warnText()} Delegate - unused import", true),
            LintError(1, 1, ruleId, "${Warnings.UNUSED_IMPORT.warnText()} com.example.equals - unused import", true)
        )
    }

    // Fixme: This test is not passing because for now we don't have type resolution
    @Disabled
    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `check by #4 should trigger`() {
        lintMethod(
            """
                |package org.cqfn.diktat.example
                |
                |import com.example.get
                |import com.example.invoke
                |import com.example.set
                |import tasks.getValue
                |
                |fun main() {
                |   val a
                |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${Warnings.UNUSED_IMPORT.warnText()} tasks.getValue - unused import", true)
        )
    }
}
