package org.cqfn.diktat.ruleset.chapter5

import org.cqfn.diktat.ruleset.constants.Warnings.CUSTOM_LABEL
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter5.CustomLabel
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CustomLabelsTest : LintTestBase(::CustomLabel) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${CustomLabel.nameId}"

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
            LintError(4, 39, ruleId, "${CUSTOM_LABEL.warnText()} @qwe", false),
            LintError(16, 34, ruleId, "${CUSTOM_LABEL.warnText()} @qq", false)
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
