package com.saveourtool.diktat.ruleset.chapter5

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings.CUSTOM_LABEL
import com.saveourtool.diktat.ruleset.rules.chapter5.CustomLabel
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CustomLabelsTest : LintTestBase(::CustomLabel) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${CustomLabel.NAME_ID}"

    @Test
    @Tag(WarningNames.CUSTOM_LABEL)
    fun `should trigger custom label`() {
        lintMethod(
            """
                    fun foo() {
                        run qwe@ {
                            q.forEach {
                                return@qwe
                            }
                        }
                        q.forEachIndexed { index, i ->
                            return@forEachIndexed
                        }
                        loop@ for(i: Int in q) {
                            println(i)
                            break@loop
                        }
                        qq@ for(i: Int in q) {
                            println(i)
                            break@qq
                        }
                        q.run {
                            it.map {
                                it.foreach{
                                    break@forEach
                                }
                            }
                        }
                    }
            """.trimMargin(),
            DiktatError(4, 39, ruleId, "${CUSTOM_LABEL.warnText()} @qwe", false),
            DiktatError(16, 34, ruleId, "${CUSTOM_LABEL.warnText()} @qq", false)
        )
    }

    @Test
    @Tag(WarningNames.CUSTOM_LABEL)
    fun `should not trigger custom label in nested expression`() {
        lintMethod(
            """
                fun foo() {
                    qq@ for(i: Int in q) {
                        for (j: Int in q) {
                            println(i)
                            break@qq
                        }
                    }

                    q.forEach outer@ {
                        it.forEach {
                            if(it == 21)
                                return@outer
                        }
                    }
                }
            """.trimMargin()
        )
    }
}
