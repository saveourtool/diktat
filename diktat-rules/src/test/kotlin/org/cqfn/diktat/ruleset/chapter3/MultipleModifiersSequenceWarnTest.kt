package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_MULTIPLE_MODIFIERS_ORDER
import org.cqfn.diktat.ruleset.rules.chapter3.MultipleModifiersSequence
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class MultipleModifiersSequenceWarnTest : LintTestBase(::MultipleModifiersSequence) {
    private val ruleId = MultipleModifiersSequence.NAME_ID

    @Test
    @Tag(WarningNames.WRONG_MULTIPLE_MODIFIERS_ORDER)
    fun `check wrong order modifier in fun and variable with annotation`() {
        lintMethod(
            """
                    |@Annotation
                    |final public fun foo() {
                    |   lateinit open protected var a: List<ASTNode>
                    |}
            """.trimMargin(),
            LintError(2, 1, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} final should be on position 2, but is on position 1", true),
            LintError(2, 7, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} public should be on position 1, but is on position 2", true),
            LintError(3, 4, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} lateinit should be on position 3, but is on position 1", true),
            LintError(3, 18, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} protected should be on position 1, but is on position 3", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_MULTIPLE_MODIFIERS_ORDER)
    fun `check correct order modifier in fun and variable and without`() {
        lintMethod(
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
        lintMethod(
            """
                    |inline tailrec public fun qwe(vararg text: String) {}
                    |
                    |inline suspend fun f(crossinline body: () -> Unit) {}
                    |
                    |inline  fun < reified T> membersOf() = T::class.members
            """.trimMargin(),
            LintError(1, 1, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} inline should be on position 3, but is on position 1", true),
            LintError(1, 16, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} public should be on position 1, but is on position 3", true),
            LintError(3, 1, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} inline should be on position 2, but is on position 1", true),
            LintError(3, 8, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} suspend should be on position 1, but is on position 2", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_MULTIPLE_MODIFIERS_ORDER)
    fun `check wrong order modifier in class`() {
        lintMethod(
            """
                    |enum public class Q {}
                    |
                    |data protected  class Counter(val dayIndex: Int) {
                    |   operator suspend fun plus(increment: Int): Counter {
                    |      return Counter(dayIndex + increment)
                    |      }
                    |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} enum should be on position 2, but is on position 1", true),
            LintError(1, 6, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} public should be on position 1, but is on position 2", true),
            LintError(3, 1, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} data should be on position 2, but is on position 1", true),
            LintError(3, 6, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} protected should be on position 1, but is on position 2", true),
            LintError(4, 4, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} operator should be on position 2, but is on position 1", true),
            LintError(4, 13, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} suspend should be on position 1, but is on position 2", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_MULTIPLE_MODIFIERS_ORDER)
    fun `check wrong annotation order`() {
        lintMethod(
            """
                    |public @Annotation final fun foo() {
                    |}
            """.trimMargin(),
            LintError(1, 8, ruleId, "${WRONG_MULTIPLE_MODIFIERS_ORDER.warnText()} @Annotation annotation should be before all modifiers", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_MULTIPLE_MODIFIERS_ORDER)
    fun `check correct order modifier for value`() {
        lintMethod(
            """
                    |public value class Foo() {
                    |}
            """.trimMargin(),
        )
    }
}
