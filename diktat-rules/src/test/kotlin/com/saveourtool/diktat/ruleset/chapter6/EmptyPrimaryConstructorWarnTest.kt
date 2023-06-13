package com.saveourtool.diktat.ruleset.chapter6

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings.EMPTY_PRIMARY_CONSTRUCTOR
import com.saveourtool.diktat.ruleset.rules.chapter6.AvoidEmptyPrimaryConstructor
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class EmptyPrimaryConstructorWarnTest : LintTestBase(::AvoidEmptyPrimaryConstructor) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${AvoidEmptyPrimaryConstructor.NAME_ID}"

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
            DiktatError(1, 1, ruleId, "${EMPTY_PRIMARY_CONSTRUCTOR.warnText()} Some", true),
            DiktatError(8, 1, ruleId, "${EMPTY_PRIMARY_CONSTRUCTOR.warnText()} Some1", true)
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
