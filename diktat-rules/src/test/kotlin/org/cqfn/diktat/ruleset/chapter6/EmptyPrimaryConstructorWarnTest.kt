package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.constants.Warnings.EMPTY_PRIMARY_CONSTRUCTOR
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter6.AvoidEmptyPrimaryConstructor
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class EmptyPrimaryConstructorWarnTest : LintTestBase(::AvoidEmptyPrimaryConstructor) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${AvoidEmptyPrimaryConstructor.nameId}"

    @Test
    @Tag(WarningNames.EMPTY_PRIMARY_CONSTRUCTOR)
    fun `simple classes with empty primary constructor`() {
        lintMethod(
            """
                    |class Some() {
                    |   val a = 10
                    |   constructor(a: String): this() {
                    |       this.a = a
                    |   }
                    |}
                    |
                    |class Some1() {
                    |   val a = 10
                    |   companion object {}
                    |}
                    |
                    |class Some2 {
                    |   val a = 10
                    |   constructor(a: String): this() {
                    |       this.a = a
                    |   }
                    |}
                    |
                    |class Some3 private constructor () {
                    |
                    |}
                """.trimMargin(),
            LintError(1, 1, ruleId, "${EMPTY_PRIMARY_CONSTRUCTOR.warnText()} Some", true),
            LintError(8, 1, ruleId, "${EMPTY_PRIMARY_CONSTRUCTOR.warnText()} Some1", true)
        )
    }

    @Test
    @Tag(WarningNames.EMPTY_PRIMARY_CONSTRUCTOR)
    fun `correct example with empty primary constructor and modifiers`() {
        lintMethod(
            """
                    |class Some1 private constructor () {
                    |
                    |}
                    |
                    |class Some2 @Inject constructor() {
                    |}
                """.trimMargin()
        )
    }
}
