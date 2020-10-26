package org.cqfn.diktat.ruleset.chapter6

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings.USELESS_OVERRIDE
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.UselessOverride
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class UselessOverrideWarnTest: LintTestBase(::UselessOverride) {

    private val ruleId = "$DIKTAT_RULE_SET_ID:useless-override"

    @Test
    @Tag(WarningNames.USELESS_OVERRIDE)
    fun `check simple wrong examples`() {
        lintMethod(
                """
                    open class Rectangle {
                        open fun draw() { /* ... */ }
                    }
                    
                    class Square() : Rectangle() {
                        override fun draw() {
                        /**
                         *
                         * hehe
                         */
                            super<Rectangle>.draw()
                        }
                    }
                    
                    class Square2() : Rectangle() {
                        override fun draw() {
                            //hehe
                            /*
                                hehe
                            */
                            super<Rectangle>.draw()
                        }
                    }
                    
                    class Square2() : Rectangle() {
                        override fun draw() {
                            val q = super.draw()
                        }
                    }
                    
                    class A: Runnable {
                        override fun run() {
                        
                        }
                    }
                """.trimMargin(),
                LintError(6,25, ruleId, "${USELESS_OVERRIDE.warnText()} draw", true),
                LintError(16,25, ruleId, "${USELESS_OVERRIDE.warnText()} draw", true)
        )
    }
}
