package com.saveourtool.diktat.ruleset.chapter6

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings.USELESS_SUPERTYPE
import com.saveourtool.diktat.ruleset.rules.chapter6.UselessSupertype
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class UselessSupertypeWarnTest : LintTestBase(::UselessSupertype) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${UselessSupertype.NAME_ID}"

    @Test
    @Tag(WarningNames.USELESS_SUPERTYPE)
    @Suppress("TOO_LONG_FUNCTION")
    fun `check simple wrong example`() {
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
            DiktatError(11, 35, ruleId, "${USELESS_SUPERTYPE.warnText()} Rectangle", true),
            DiktatError(21, 35, ruleId, "${USELESS_SUPERTYPE.warnText()} Rectangle", true)
        )
    }

    @Test
    @Tag(WarningNames.USELESS_SUPERTYPE)
    fun `check example with two super`() {
        lintMethod(
            """
                    open class Rectangle {
                        open fun draw() { /* ... */ }
                    }

                    interface KK {
                        fun draw() {}
                        fun kk() {}
                    }

                    class Square2() : Rectangle(), KK {
                        override fun draw() {
                            super<Rectangle>.draw()
                            super<KK>.draw()
                        }

                        private fun goo() {
                            super<KK>.kk()
                        }

                    }
            """.trimMargin(),
            DiktatError(17, 35, ruleId, "${USELESS_SUPERTYPE.warnText()} KK", true)
        )
    }
}
