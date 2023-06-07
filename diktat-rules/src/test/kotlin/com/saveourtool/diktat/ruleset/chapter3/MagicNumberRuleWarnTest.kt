package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.MAGIC_NUMBER
import com.saveourtool.diktat.ruleset.rules.chapter3.MagicNumberRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class MagicNumberRuleWarnTest : LintTestBase(::MagicNumberRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${MagicNumberRule.NAME_ID}"
    private val rulesConfigMagicNumber: List<RulesConfig> = listOf(
        RulesConfig(
            MAGIC_NUMBER.name, true,
            mapOf(
                "ignoreHashCodeFunction" to "true",
                "ignorePropertyDeclaration" to "true",
                "ignoreLocalVariableDeclaration" to "true",
                "ignoreValueParameter" to "false",
                "ignoreConstantDeclaration" to "true",
                "ignoreCompanionObjectPropertyDeclaration" to "true",
                "ignoreEnums" to "true",
                "ignoreRanges" to "true",
                "ignoreExtensionFunctions" to "true"))
    )
    private val rulesConfigIgnoreNumbersMagicNumber: List<RulesConfig> = listOf(
        RulesConfig(
            MAGIC_NUMBER.name, true,
            mapOf(
                "ignoreNumbers" to "50,-240, 128L, -3.5f"))
    )

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `simple check`() {
        lintMethod(
            """
                |fun f1oo() {
                |   val a: Byte = 4
                |   val b = 128L
                |   val e = 3.4f
                |   val g = 4/3
                |   val h = 0U
                |   val r = 1UL
                |   val f = "qwe\$\{12\}hhe"
                |}
                |
                |@Override
                |fun hashCode(): Int {
                |   return 32
                |}
                |
                |class Cl{
                |   companion object {
                |       private const val AA = 4
                |   }
                |}
            """.trimMargin(),
            DiktatError(2, 18, ruleId, "${MAGIC_NUMBER.warnText()} 4", false),
            DiktatError(3, 12, ruleId, "${MAGIC_NUMBER.warnText()} 128L", false),
            DiktatError(4, 12, ruleId, "${MAGIC_NUMBER.warnText()} 3.4f", false),
            DiktatError(5, 12, ruleId, "${MAGIC_NUMBER.warnText()} 4", false),
            DiktatError(5, 14, ruleId, "${MAGIC_NUMBER.warnText()} 3", false)
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check ignore numbers`() {
        lintMethod(
            """
                |fun f1oo() {
                |   val m = -1
                |   val a: Byte = 4
                |   val b = 0xff
                |}
                |
                |enum class A(b:Int) {
                |   A(-240),
                |   B(50),
                |   C(3),
                |}
                |@Override
                |fun hashCode(): Int {
                |   return 32
                |}
            """.trimMargin(),
            DiktatError(2, 13, ruleId, "${MAGIC_NUMBER.warnText()} -1", false),
            DiktatError(3, 18, ruleId, "${MAGIC_NUMBER.warnText()} 4", false),
            DiktatError(4, 12, ruleId, "${MAGIC_NUMBER.warnText()} 0xff", false),
            DiktatError(10, 6, ruleId, "${MAGIC_NUMBER.warnText()} 3", false),
            rulesConfigList = rulesConfigIgnoreNumbersMagicNumber
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check ignore top level constants`() {
        lintMethod(
            """
                |const val topLevel = 31
                |
                |val shouldTrigger = 32
                |
                |fun some() {
                |
                |}
            """.trimMargin(),
            DiktatError(3, 21, ruleId, "${MAGIC_NUMBER.warnText()} 32", false),
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check all param in config true`() {
        lintMethod(
            """
                |fun foo() {
                |   var a = 3
                |}
                |
                |const val aa = 21.5f
                |
                |class A {
                |   companion object {
                |       val b = 2
                |   }
                |}
                |
                |enum class A(b:Int) {
                |   A(1),
                |   B(2),
                |   C(3),
                |}
                |
                |fun goo() {
                |   val q = 100.1000
                |}
                |
                |fun Int.foo() = 2
            """.trimMargin(), rulesConfigList = rulesConfigMagicNumber
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check value parameter`() {
        lintMethod(
            """
                class TomlDecoder(
                    var elementsCount: Int = 100
                )
            """.trimMargin(),
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check value parameter with config`() {
        lintMethod(
            """
                class TomlDecoder(
                    var elementsCount: Int = 100
                )
            """.trimMargin(),
            DiktatError(2, 46, ruleId, "${MAGIC_NUMBER.warnText()} 100", false),
            rulesConfigList = rulesConfigMagicNumber
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check value parameter in function with config`() {
        lintMethod(
            """
                fun TomlDecoder(elementsCount: Int = 100) {}
            """.trimMargin(),
            DiktatError(1, 54, ruleId, "${MAGIC_NUMBER.warnText()} 100", false),
            rulesConfigList = rulesConfigMagicNumber
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check ignore numbers in test`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                |fun f1oo() {
                |   val m = -1
                |   val a: Byte = 4
                |   val b = 0xff
                |}
                |
                |enum class A(b:Int) {
                |   A(-240),
                |   B(50),
                |   C(3),
                |}
                |@Override
                |fun hashCode(): Int {
                |   return 32
                |}
            """.trimMargin(),
            tempDir = tempDir,
            fileName = "src/test/kotlin/org/cqfn/diktat/test/hehe/MagicNumberTest.kt",
            rulesConfigList = rulesConfigIgnoreNumbersMagicNumber,
        )
    }
}
