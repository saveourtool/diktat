package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.constants.Warnings.FILE_CONTAINS_ONLY_COMMENTS
import com.saveourtool.diktat.ruleset.constants.Warnings.FILE_INCORRECT_BLOCKS_ORDER
import com.saveourtool.diktat.ruleset.constants.Warnings.FILE_NO_BLANK_LINE_BETWEEN_BLOCKS
import com.saveourtool.diktat.ruleset.constants.Warnings.FILE_UNORDERED_IMPORTS
import com.saveourtool.diktat.ruleset.constants.Warnings.FILE_WILDCARD_IMPORTS
import com.saveourtool.diktat.ruleset.rules.chapter3.files.FileStructureRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class FileStructureRuleTest : LintTestBase(::FileStructureRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${FileStructureRule.NAME_ID}"
    private val rulesConfigListWildCardImport: List<RulesConfig> = listOf(
        RulesConfig(FILE_WILDCARD_IMPORTS.name, true,
            mapOf("allowedWildcards" to "com.saveourtool.diktat.*"))
    )
    private val rulesConfigListWildCardImports: List<RulesConfig> = listOf(
        RulesConfig(FILE_WILDCARD_IMPORTS.name, true,
            mapOf("allowedWildcards" to "com.saveourtool.diktat.*, com.saveourtool.diktat.ruleset.constants.Warnings.*"))
    )
    private val rulesConfigListEmptyDomainName: List<RulesConfig> = listOf(
        RulesConfig("DIKTAT_COMMON", true, mapOf("domainName" to ""))
    )

    @Test
    @Tag(WarningNames.FILE_CONTAINS_ONLY_COMMENTS)
    fun `should warn if file contains only comments`() {
        lintMethod(
            """
                |package com.saveourtool.diktat.example
                |
                |/**
                | * This file appears to be empty
                | */
                |
                |
                |// lorem ipsum
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${FILE_CONTAINS_ONLY_COMMENTS.warnText()} file contains no code", false)
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
                |package com.saveourtool.diktat.example
                |
                |class Example { }
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${FILE_INCORRECT_BLOCKS_ORDER.warnText()} @file:JvmName(\"Foo\")", true),
            DiktatError(3, 1, ruleId, "${FILE_INCORRECT_BLOCKS_ORDER.warnText()} /**", true)
        )
    }

    @Test
    @Tag(WarningNames.FILE_UNORDERED_IMPORTS)
    fun `should warn if imports are not sorted alphabetically`() {
        lintMethod(
            """
                |package com.saveourtool.diktat.example
                |
                |import org.junit.Test
                |import com.saveourtool.diktat.Foo
                |
                |class Example {
                |val x: Test = null
                |val y: Foo = null
                |}
            """.trimMargin(),
            DiktatError(3, 1, ruleId, "${FILE_UNORDERED_IMPORTS.warnText()} import com.saveourtool.diktat.Foo...", true)
        )
    }

    @Test
    @Tag(WarningNames.FILE_WILDCARD_IMPORTS)
    fun `should warn if wildcard imports are used`() {
        lintMethod(
            """
                |package com.saveourtool.diktat.example
                |
                |import com.saveourtool.diktat.*
                |
                |class Example { }
            """.trimMargin(),
            DiktatError(3, 1, ruleId, "${FILE_WILDCARD_IMPORTS.warnText()} import com.saveourtool.diktat.*", false),
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
                |package com.saveourtool.diktat.example
                |import com.saveourtool.diktat.Foo
                |class Example{
                |val x: Foo = null
                |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${FILE_NO_BLANK_LINE_BETWEEN_BLOCKS.warnText()} /**", true),
            DiktatError(4, 1, ruleId, "${FILE_NO_BLANK_LINE_BETWEEN_BLOCKS.warnText()} @file:JvmName(\"Foo\")", true),
            DiktatError(7, 1, ruleId, "${FILE_NO_BLANK_LINE_BETWEEN_BLOCKS.warnText()} package com.saveourtool.diktat.example", true),
            DiktatError(8, 1, ruleId, "${FILE_NO_BLANK_LINE_BETWEEN_BLOCKS.warnText()} import com.saveourtool.diktat.Foo", true)
        )
    }

    @Test
    @Tag(WarningNames.FILE_WILDCARD_IMPORTS)
    fun `wildcard imports are used but with config`() {
        lintMethod(
            """
                |package com.saveourtool.diktat.example
                |
                |import com.saveourtool.diktat.*
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
                |package com.saveourtool.diktat.example
                |
                |import com.saveourtool.diktat.*
                |import com.saveourtool.diktat.ruleset.constants.Warnings.*
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
                |package com.saveourtool.diktat.example
                |
                |import com.saveourtool.diktat.Foo
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
                |package com.saveourtool.diktat.example
                |
                |import com.saveourtool.diktat.Foo
                |
                |class Example{
                |val x: Foo = null
                |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${FILE_INCORRECT_BLOCKS_ORDER.warnText()} // some notes on this file", true),
            DiktatError(2, 1, ruleId, "${FILE_INCORRECT_BLOCKS_ORDER.warnText()} /**", true),
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
                |package com.saveourtool.diktat.example
                |
                |import com.saveourtool.diktat.Foo
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
                |package com.saveourtool.diktat.example
                |
                |import com.saveourtool.diktat.Foo
                |
                |class Example{
                |val x: Foo = null
                |}
            """.trimMargin(),
            DiktatError(4, 1, ruleId, "${FILE_INCORRECT_BLOCKS_ORDER.warnText()} @file:Annotation", true)
        )
    }

    @Test
    @Tag(WarningNames.FILE_INCORRECT_BLOCKS_ORDER)
    fun `test with empty domain name in config`() {
        lintMethod(
            """
                |package com.saveourtool.diktat.example
                |
                |import com.saveourtool.diktat.Foo
                |import com.pinterest.ktlint.core.LintError
                |
                |class Example{
                |val x: LintError = null
                |val x: Foo = null
                |}
            """.trimMargin(),
            DiktatError(3, 1, ruleId, "${FILE_UNORDERED_IMPORTS.warnText()} import com.pinterest.ktlint.core.LintError...", true),
            rulesConfigList = rulesConfigListEmptyDomainName
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `import from the package`() {
        lintMethod(
            """
                |package com.saveourtool.diktat.example
                |
                |import com.saveourtool.diktat.example.Foo
                |
                |class Example {
                |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${Warnings.UNUSED_IMPORT.warnText()} com.saveourtool.diktat.example.Foo - unused import", true)
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `unused import`() {
        lintMethod(
            """
                |package com.saveourtool.diktat.example
                |
                |import com.saveourtool.diktat.Foo
                |
                |class Example {
                |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${Warnings.UNUSED_IMPORT.warnText()} com.saveourtool.diktat.Foo - unused import", true)
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `used import`() {
        lintMethod(
            """
                |package com.saveourtool.diktat.example
                |
                |import com.saveourtool.diktat.Foo
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
                |package com.saveourtool.diktat.example
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
                |package com.saveourtool.diktat.example
                |
                |import com.saveourtool.diktat.utils.logAndExit
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
                |package com.saveourtool.diktat.example
                |
                |import com.saveourtool.diktat.utils.logAndExit
                |
                |fun main() {
                |println("Type is not supported yet")
                |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${Warnings.UNUSED_IMPORT.warnText()} com.saveourtool.diktat.utils.logAndExit - unused import", true)
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `Acute import`() {
        lintMethod(
            """
                |package com.saveourtool.diktat.example
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
                |package com.saveourtool.diktat.example
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
                |package com.saveourtool.diktat.example
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
                |package com.saveourtool.diktat.example
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
                |package com.saveourtool.diktat.example
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
            DiktatError(1, 1, ruleId, "${Warnings.UNUSED_IMPORT.warnText()} Delegate - unused import", true),
            DiktatError(1, 1, ruleId, "${Warnings.UNUSED_IMPORT.warnText()} com.example.equals - unused import", true)
        )
    }

    // Fixme: This test is not passing because for now we don't have type resolution
    @Disabled
    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `check by #4 should trigger`() {
        lintMethod(
            """
                |package com.saveourtool.diktat.example
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
            DiktatError(1, 1, ruleId, "${Warnings.UNUSED_IMPORT.warnText()} tasks.getValue - unused import", true)
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `import in KDoc #1`() {
        lintMethod(
            """
                |import java.io.IOException
                |
                |interface BluetoothApi {
                |
                |    /**
                |     * Send array of bytes to bluetooth output stream.
                |     * This call is asynchronous.
                |     *
                |     * Note that this operation can still throw an [IOException] if the remote device silently
                |     * closes the connection so the pipe gets broken.
                |     *
                |     * @param bytes data to send
                |     * @return true if success, false if there was an error or device has been disconnected
                |     */
                |    fun trySend(bytes: ByteArray): Boolean
                |}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `import in KDoc #2`() {
        lintMethod(
            """
                |import java.io.IOException
                |import java.io.IOException as IOE
                |import java.io.UncheckedIOException
                |import java.io.UncheckedIOException as UIOE
                |
                |interface BluetoothApi {
                |    /**
                |     * @see IOException
                |     * @see [UncheckedIOException]
                |     * @see IOE
                |     * @see [UIOE]
                |     */
                |    fun trySend(bytes: ByteArray): Boolean
                |}
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.UNUSED_IMPORT)
    fun `import in KDoc #3`() {
        lintMethod(
            """
                |package com.example
                |
                |import com.example.Library1 as Lib1
                |import com.example.Library1.doSmth as doSmthElse1
                |import com.example.Library2 as Lib2
                |import com.example.Library2.doSmth as doSmthElse2
                |
                |object Library1 {
                |    fun doSmth(): Unit = TODO()
                |}
                |
                |object Library2 {
                |    fun doSmth(): Unit = TODO()
                |}
                |
                |/**
                | * @see Lib1.doSmth
                | * @see doSmthElse1
                | * @see [Lib2.doSmth]
                | * @see [doSmthElse2]
                | */
                |class Client
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.FILE_INCORRECT_BLOCKS_ORDER)
    fun `error in moving blocking at the first place`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                // Without suppressing these, version catalog usage in `plugins` is marked as an error in IntelliJ:
                // https://youtrack.jetbrains.com/issue/KTIJ-19369
                @file:Suppress("DSL_SCOPE_VIOLATION")
                plugins {
                    id(libs.plugins.kotlinJvm.get().pluginId)
                }
            """.trimIndent(),
            fileName = "build.gradle.kts",
            tempDir = tempDir,
            expectedLintErrors = arrayOf(DiktatError(3, 1, ruleId, "${Warnings.FILE_INCORRECT_BLOCKS_ORDER.warnText()} @file:Suppress(\"DSL_SCOPE_VIOLATION\")", true)),
        )
    }
}
