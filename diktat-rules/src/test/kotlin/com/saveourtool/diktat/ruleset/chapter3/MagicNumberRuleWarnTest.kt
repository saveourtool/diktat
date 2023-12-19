package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.api.DiktatError
import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.MAGIC_NUMBER
import com.saveourtool.diktat.ruleset.rules.chapter3.MagicNumberRule
import com.saveourtool.diktat.util.LintTestBase
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class MagicNumberRuleWarnTest : LintTestBase(::MagicNumberRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${MagicNumberRule.NAME_ID}"
    private val rulesConfigIgnoreNone: List<RulesConfig> = listOf(
        RulesConfig(
            MAGIC_NUMBER.name, true,
            mapOf(
                "ignoreHashCodeFunction" to "false",
                "ignorePropertyDeclaration" to "false",
                "ignoreLocalVariableDeclaration" to "false",
                "ignoreValueParameter" to "false",
                "ignoreConstantDeclaration" to "false",
                "ignoreCompanionObjectPropertyDeclaration" to "false",
                "ignoreEnums" to "false",
                "ignoreRanges" to "false",
                "ignoreExtensionFunctions" to "false",
                "ignorePairsCreatedUsingTo" to "false"))
    )
    private val rulesConfigIgnoreNumbers: List<RulesConfig> = listOf(
        RulesConfig(
            MAGIC_NUMBER.name, true,
            mapOf(
                "ignoreNumbers" to "50,-240, 128L, -3.5f, 4, 11UL",
                "ignoreHashCodeFunction" to "false",
                "ignorePropertyDeclaration" to "false",
                "ignoreLocalVariableDeclaration" to "false",
                "ignoreValueParameter" to "false",
                "ignoreConstantDeclaration" to "false",
                "ignoreCompanionObjectPropertyDeclaration" to "false",
                "ignoreEnums" to "false",
                "ignoreRanges" to "false",
                "ignoreExtensionFunctions" to "false",
                "ignorePairsCreatedUsingTo" to "false"))
    )
    private val rulesConfigIgnoreHashCodeFunction: List<RulesConfig> = listOf(
        RulesConfig(
            MAGIC_NUMBER.name, true,
            mapOf(
                "ignoreHashCodeFunction" to "true",
                "ignorePropertyDeclaration" to "false",
                "ignoreLocalVariableDeclaration" to "false",
                "ignoreValueParameter" to "false",
                "ignoreConstantDeclaration" to "false",
                "ignoreCompanionObjectPropertyDeclaration" to "false",
                "ignoreEnums" to "false",
                "ignoreRanges" to "false",
                "ignoreExtensionFunctions" to "false",
                "ignorePairsCreatedUsingTo" to "false"))
    )
    private val rulesConfigIgnorePropertyDeclaration: List<RulesConfig> = listOf(
        RulesConfig(
            MAGIC_NUMBER.name, true,
            mapOf(
                "ignoreHashCodeFunction" to "false",
                "ignorePropertyDeclaration" to "true",
                "ignoreLocalVariableDeclaration" to "false",
                "ignoreValueParameter" to "false",
                "ignoreConstantDeclaration" to "false",
                "ignoreCompanionObjectPropertyDeclaration" to "false",
                "ignoreEnums" to "false",
                "ignoreRanges" to "false",
                "ignoreExtensionFunctions" to "false",
                "ignorePairsCreatedUsingTo" to "false"))
    )
    private val rulesConfigIgnoreLocalVariableDeclaration: List<RulesConfig> = listOf(
        RulesConfig(
            MAGIC_NUMBER.name, true,
            mapOf(
                "ignoreHashCodeFunction" to "false",
                "ignorePropertyDeclaration" to "false",
                "ignoreLocalVariableDeclaration" to "true",
                "ignoreValueParameter" to "false",
                "ignoreConstantDeclaration" to "false",
                "ignoreCompanionObjectPropertyDeclaration" to "false",
                "ignoreEnums" to "false",
                "ignoreRanges" to "false",
                "ignoreExtensionFunctions" to "false",
                "ignorePairsCreatedUsingTo" to "false"))
    )
    private val rulesConfigIgnoreValueParameter: List<RulesConfig> = listOf(
        RulesConfig(
            MAGIC_NUMBER.name, true,
            mapOf(
                "ignoreHashCodeFunction" to "false",
                "ignorePropertyDeclaration" to "false",
                "ignoreLocalVariableDeclaration" to "false",
                "ignoreValueParameter" to "true",
                "ignoreConstantDeclaration" to "false",
                "ignoreCompanionObjectPropertyDeclaration" to "false",
                "ignoreEnums" to "false",
                "ignoreRanges" to "false",
                "ignoreExtensionFunctions" to "false",
                "ignorePairsCreatedUsingTo" to "false"))
    )
    private val rulesConfigIgnoreConstantDeclaration: List<RulesConfig> = listOf(
        RulesConfig(
            MAGIC_NUMBER.name, true,
            mapOf(
                "ignoreHashCodeFunction" to "false",
                "ignorePropertyDeclaration" to "false",
                "ignoreLocalVariableDeclaration" to "false",
                "ignoreValueParameter" to "false",
                "ignoreConstantDeclaration" to "true",
                "ignoreCompanionObjectPropertyDeclaration" to "false",
                "ignoreEnums" to "false",
                "ignoreRanges" to "false",
                "ignoreExtensionFunctions" to "false",
                "ignorePairsCreatedUsingTo" to "false"))
    )
    private val rulesConfigIgnoreCompanionObjectPropertyDeclaration: List<RulesConfig> = listOf(
        RulesConfig(
            MAGIC_NUMBER.name, true,
            mapOf(
                "ignoreHashCodeFunction" to "false",
                "ignorePropertyDeclaration" to "false",
                "ignoreLocalVariableDeclaration" to "false",
                "ignoreValueParameter" to "false",
                "ignoreConstantDeclaration" to "false",
                "ignoreCompanionObjectPropertyDeclaration" to "true",
                "ignoreEnums" to "false",
                "ignoreRanges" to "false",
                "ignoreExtensionFunctions" to "false",
                "ignorePairsCreatedUsingTo" to "false"))
    )
    private val rulesConfigIgnoreEnums: List<RulesConfig> = listOf(
        RulesConfig(
            MAGIC_NUMBER.name, true,
            mapOf(
                "ignoreHashCodeFunction" to "false",
                "ignorePropertyDeclaration" to "false",
                "ignoreLocalVariableDeclaration" to "false",
                "ignoreValueParameter" to "false",
                "ignoreConstantDeclaration" to "false",
                "ignoreCompanionObjectPropertyDeclaration" to "false",
                "ignoreEnums" to "true",
                "ignoreRanges" to "false",
                "ignoreExtensionFunctions" to "false",
                "ignorePairsCreatedUsingTo" to "false"))
    )
    private val rulesConfigIgnoreRanges: List<RulesConfig> = listOf(
        RulesConfig(
            MAGIC_NUMBER.name, true,
            mapOf(
                "ignoreHashCodeFunction" to "false",
                "ignorePropertyDeclaration" to "false",
                "ignoreLocalVariableDeclaration" to "false",
                "ignoreValueParameter" to "false",
                "ignoreConstantDeclaration" to "false",
                "ignoreCompanionObjectPropertyDeclaration" to "false",
                "ignoreEnums" to "false",
                "ignoreRanges" to "true",
                "ignoreExtensionFunctions" to "false",
                "ignorePairsCreatedUsingTo" to "false"))
    )
    private val rulesConfigIgnoreExtensionFunctions: List<RulesConfig> = listOf(
        RulesConfig(
            MAGIC_NUMBER.name, true,
            mapOf(
                "ignoreHashCodeFunction" to "false",
                "ignorePropertyDeclaration" to "false",
                "ignoreLocalVariableDeclaration" to "false",
                "ignoreValueParameter" to "false",
                "ignoreConstantDeclaration" to "false",
                "ignoreCompanionObjectPropertyDeclaration" to "false",
                "ignoreEnums" to "false",
                "ignoreRanges" to "false",
                "ignoreExtensionFunctions" to "true",
                "ignorePairsCreatedUsingTo" to "false"))
    )
    private val rulesConfigIgnorePairsCreatedUsingTo: List<RulesConfig> = listOf(
        RulesConfig(
            MAGIC_NUMBER.name, true,
            mapOf(
                "ignoreHashCodeFunction" to "false",
                "ignorePropertyDeclaration" to "false",
                "ignoreLocalVariableDeclaration" to "false",
                "ignoreValueParameter" to "false",
                "ignoreConstantDeclaration" to "false",
                "ignoreCompanionObjectPropertyDeclaration" to "false",
                "ignoreEnums" to "false",
                "ignoreRanges" to "false",
                "ignoreExtensionFunctions" to "false",
                "ignorePairsCreatedUsingTo" to "true"))
    )

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    @Suppress("LongMethod")
    fun `check all`() {
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
                |val abc = 32
                |var abc2 = 32
                |
                |fun foo() {
                |   val a = 3
                |   var a2 = 3
                |}
                |
                |class TomlDecoder(
                |    val elementsCount: Int = 100,
                |    var elementsCount2: Int = 100
                |)
                |
                |fun TomlDecoder(elementsCount: Int = 100) {}
                |
                |const val topLevel = 31
                |
                |class A {
                |   companion object {
                |       val b = 3
                |       var b2 = 4
                |   }
                |}
                |
                |enum class A(b:Int) {
                |   A(3),
                |   B(4),
                |   C(5),
                |}
                |
                |val tagLengthRange = 3..15
                |var tagLengthRange2 = 3..15
                |
                |fun Int.foo() = 3
                |
                |val fg = abc to 3
                |var fg2 = abc to 4
            """.trimMargin(),
            DiktatError(2, 18, ruleId, "${MAGIC_NUMBER.warnText()} 4", false),
            DiktatError(3, 12, ruleId, "${MAGIC_NUMBER.warnText()} 128L", false),
            DiktatError(4, 12, ruleId, "${MAGIC_NUMBER.warnText()} 3.4f", false),
            DiktatError(5, 12, ruleId, "${MAGIC_NUMBER.warnText()} 4", false),
            DiktatError(5, 14, ruleId, "${MAGIC_NUMBER.warnText()} 3", false),
            DiktatError(13, 11, ruleId, "${MAGIC_NUMBER.warnText()} 32", false),
            DiktatError(16, 11, ruleId, "${MAGIC_NUMBER.warnText()} 32", false),
            DiktatError(17, 12, ruleId, "${MAGIC_NUMBER.warnText()} 32", false),
            DiktatError(20, 12, ruleId, "${MAGIC_NUMBER.warnText()} 3", false),
            DiktatError(21, 13, ruleId, "${MAGIC_NUMBER.warnText()} 3", false),
            DiktatError(25, 30, ruleId, "${MAGIC_NUMBER.warnText()} 100", false),
            DiktatError(26, 31, ruleId, "${MAGIC_NUMBER.warnText()} 100", false),
            DiktatError(29, 38, ruleId, "${MAGIC_NUMBER.warnText()} 100", false),
            DiktatError(31, 22, ruleId, "${MAGIC_NUMBER.warnText()} 31", false),
            DiktatError(35, 16, ruleId, "${MAGIC_NUMBER.warnText()} 3", false),
            DiktatError(36, 17, ruleId, "${MAGIC_NUMBER.warnText()} 4", false),
            DiktatError(41, 6, ruleId, "${MAGIC_NUMBER.warnText()} 3", false),
            DiktatError(42, 6, ruleId, "${MAGIC_NUMBER.warnText()} 4", false),
            DiktatError(43, 6, ruleId, "${MAGIC_NUMBER.warnText()} 5", false),
            DiktatError(46, 22, ruleId, "${MAGIC_NUMBER.warnText()} 3", false),
            DiktatError(46, 25, ruleId, "${MAGIC_NUMBER.warnText()} 15", false),
            DiktatError(47, 23, ruleId, "${MAGIC_NUMBER.warnText()} 3", false),
            DiktatError(47, 26, ruleId, "${MAGIC_NUMBER.warnText()} 15", false),
            DiktatError(49, 17, ruleId, "${MAGIC_NUMBER.warnText()} 3", false),
            DiktatError(51, 17, ruleId, "${MAGIC_NUMBER.warnText()} 3", false),
            DiktatError(52, 18, ruleId, "${MAGIC_NUMBER.warnText()} 4", false),
            rulesConfigList = rulesConfigIgnoreNone
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check ignore numbers`() {
        lintMethod(
            """
                |fun f1oo() {
                |   val m = -1
                |   var m2 = -1
                |   val a: Byte = 4
                |   var a2: Byte = 4
                |   val b = 0xff
                |   var b2 = 0xff
                |}
                |
                |enum class A(b:Int) {
                |   A(-240),
                |   B(50),
                |   C(3),
                |}
            """.trimMargin(),
            DiktatError(2, 13, ruleId, "${MAGIC_NUMBER.warnText()} -1", false),
            DiktatError(3, 14, ruleId, "${MAGIC_NUMBER.warnText()} -1", false),
            DiktatError(6, 12, ruleId, "${MAGIC_NUMBER.warnText()} 0xff", false),
            DiktatError(7, 13, ruleId, "${MAGIC_NUMBER.warnText()} 0xff", false),
            DiktatError(13, 6, ruleId, "${MAGIC_NUMBER.warnText()} 3", false),
            rulesConfigList = rulesConfigIgnoreNumbers
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check ignore hash code function`() {
        lintMethod(
            """
                |@Override
                |fun hashCode(): Int {
                |   return 32
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigIgnoreHashCodeFunction
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check ignore property declaration`() {
        lintMethod(
            """
                |val abc = 32
                |var abc2 = 32
            """.trimMargin(),
            rulesConfigList = rulesConfigIgnorePropertyDeclaration
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check ignore local variable declaration`() {
        lintMethod(
            """
                |fun foo() {
                |   val a = 3
                |   var a2 = 3
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigIgnoreLocalVariableDeclaration
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check ignore value parameter`() {
        lintMethod(
            """
                |class TomlDecoder(
                |    val elementsCount: Int = 100,
                |    var elementsCount2: Int = 100
                |)
                |
                |fun TomlDecoder(elementsCount: Int = 100) {}
            """.trimMargin(),
            rulesConfigList = rulesConfigIgnoreValueParameter
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check ignore constant declaration`() {
        lintMethod(
            """
                |const val topLevel = 31
            """.trimMargin(),
            rulesConfigList = rulesConfigIgnoreConstantDeclaration
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check ignore companion object property declaration`() {
        lintMethod(
            """
                |class A {
                |   companion object {
                |       val b = 3
                |       var b2 = 4
                |   }
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigIgnoreCompanionObjectPropertyDeclaration
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check ignore enums`() {
        lintMethod(
            """
                |enum class A(b:Int) {
                |   A(3),
                |   B(4),
                |   C(5),
                |}
            """.trimMargin(),
            rulesConfigList = rulesConfigIgnoreEnums
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check ignore ranges`() {
        lintMethod(
            """
                |val tagLengthRange = 3..15
                |var tagLengthRange2 = 3..15
            """.trimMargin(),
            rulesConfigList = rulesConfigIgnoreRanges
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check ignore extension functions`() {
        lintMethod(
            """
                |fun Int.foo() = 3
            """.trimMargin(),
            rulesConfigList = rulesConfigIgnoreExtensionFunctions
        )
    }

    @Test
    @Tag(WarningNames.MAGIC_NUMBER)
    fun `check ignore pairs created using 'to'`() {
        lintMethod(
            """
                |val fg = abc to 3
                |var fg2 = abc to 4
            """.trimMargin(),
            rulesConfigList = rulesConfigIgnorePairsCreatedUsingTo
        )
    }
}
