package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_MULTIPLE_MODIFIERS_ORDER
import org.cqfn.diktat.ruleset.rules.MultipleModifiersSequence
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class MultipleModifiersSequenceWarnTest {

    private val ruleId = "$DIKTAT_RULE_SET_ID:multiple-modifiers"

    @Test
    @Tag(WarningNames.WRONG_MULTIPLE_MODIFIERS_ORDER)
    fun `check wrong order modifier in fun and variable`() {
        lintMethod(MultipleModifiersSequence(),
                """
                    |final public fun foo() {
                    |   lateinit open protected var a: List<ASTNode>   
                    |}
                """.trimMargin(),
                LintError(1, 1, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} this modifier is not in the right position", true),
                LintError(1, 7, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} this modifier is not in the right position", true),
                LintError(2, 4, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} this modifier is not in the right position", true),
                LintError(2, 18, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} this modifier is not in the right position", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_MULTIPLE_MODIFIERS_ORDER)
    fun `check correct order modifier in fun and variable and without`() {
        lintMethod(MultipleModifiersSequence(),
                """
                    |public final fun foo() {
                    |   protected open lateinit var a: List<ASTNode>   
                    |}
                    |
                    |fun goo() {
                    |
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_MULTIPLE_MODIFIERS_ORDER)
    fun `check wrong order another modifier in fun`() {
        lintMethod(MultipleModifiersSequence(),
                """
                    |inline tailrec public fun qwe(vararg text: String) {}
                    |
                    |inline suspend fun f(crossinline body: () -> Unit) {}
                    |
                    |inline  fun < reified T> membersOf() = T::class.members
                """.trimMargin(),
                LintError(1, 1, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} this modifier is not in the right position", true),
                LintError(1, 16, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} this modifier is not in the right position", true),
                LintError(3, 1, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} this modifier is not in the right position", true),
                LintError(3, 8, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} this modifier is not in the right position", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_MULTIPLE_MODIFIERS_ORDER)
    fun `check wrong order modifier in class`() {
        lintMethod(MultipleModifiersSequence(),
                """
                    |enum public class Q {}
                    |
                    |data protected  class Counter(val dayIndex: Int) {
                    |   operator suspend fun plus(increment: Int): Counter {
                    |      return Counter(dayIndex + increment) 
                    |      }
                    |}
                """.trimMargin(),
                LintError(1, 1, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} this modifier is not in the right position", true),
                LintError(1, 6, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} this modifier is not in the right position", true),
                LintError(3, 1, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} this modifier is not in the right position", true),
                LintError(3, 6, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} this modifier is not in the right position", true),
                LintError(4, 4, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} this modifier is not in the right position", true),
                LintError(4, 13, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} this modifier is not in the right position", true)
        )
    }
}
