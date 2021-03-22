package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.MAGIC_NUMBER
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter3.MagicNumberRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class MagicNumberRuleWarnTest : LintTestBase(::MagicNumberRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:magic-number"
    private val rulesConfigMagicNumber: List<RulesConfig> = listOf(
        RulesConfig(
            MAGIC_NUMBER.name, true,
            mapOf(
                "ignoreHashCodeFunction" to "true",
                "ignorePropertyDeclaration" to "true",
                "ignoreLocalVariableDeclaration" to "true",
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
            LintError(2, 18, ruleId, "${MAGIC_NUMBER.warnText()} 4", false),
            LintError(3, 12, ruleId, "${MAGIC_NUMBER.warnText()} 128L", false),
            LintError(4, 12, ruleId, "${MAGIC_NUMBER.warnText()} 3.4f", false),
            LintError(5, 12, ruleId, "${MAGIC_NUMBER.warnText()} 4", false),
            LintError(5, 14, ruleId, "${MAGIC_NUMBER.warnText()} 3", false)
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
            LintError(2, 13, ruleId, "${MAGIC_NUMBER.warnText()} -1", false),
            LintError(3, 18, ruleId, "${MAGIC_NUMBER.warnText()} 4", false),
            LintError(4, 12, ruleId, "${MAGIC_NUMBER.warnText()} 0xff", false),
            LintError(10, 6, ruleId, "${MAGIC_NUMBER.warnText()} 3", false),
            rulesConfigList = rulesConfigIgnoreNumbersMagicNumber
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
}
