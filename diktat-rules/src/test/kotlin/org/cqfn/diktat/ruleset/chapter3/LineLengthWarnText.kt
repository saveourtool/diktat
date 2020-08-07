package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.LONG_LINE
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.LineLength
import org.cqfn.diktat.util.lintMethod
import org.junit.Test


class LineLengthWarnText {

    private val ruleId = "$DIKTAT_RULE_SET_ID:line-length"

    private val rulesConfigListLineLength: List<RulesConfig> = listOf(
            RulesConfig(LONG_LINE.name, true,
                    mapOf("lineLength" to "155"))
    )

    @Test
    fun `check correct example`() {
        lintMethod(LineLength(),
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |import org.cqfn.diktat.ruleset.rules.LineLength
                    |import org.cqfn.diktat.util.lintMethod
                    |
                    |/**
                    |*https://www.google.com
                    |*https://www.google.com
                    |*@param a
                    |*/
                    |
                    |class A{
                    |   companion object {
                    |   }
                    |
                    |   fun foo() {
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `check correct example with long URL in KDOC`() {
        lintMethod(LineLength(),
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |import org.cqfn.diktat.ruleset.rules.LineLength
                    |import org.cqfn.diktat.util.lintMethod
                    |
                    |/**
                    |*https://www.google.com/search?q=posible+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    |*https://www.google.com/search?q=posible+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    |*@param a
                    |*/
                    |
                    |class A{
                    |   companion object {
                    |   }
                    |
                    |   fun foo() {
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `check correct example with long URL in KDOC and long import`() {
        lintMethod(LineLength(),
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |import org.cqfn.diktat.ruleset.rules.LineLength.sdfsdfsf.sdfsdfsdfsdfdghdf.gfhdf.hdstst.dh.dsgfdfgdgs.rhftheryryj.cgh
                    |import org.cqfn.diktat.util.lintMethod
                    |
                    |/**
                    |*https://www.google.com/search?q=posible+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    |*https://www.google.com/search?q=posible+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    |*@param a
                    |*/
                    |
                    |class A{
                    |   companion object {
                    |   }
                    |
                    |   fun foo() {
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `check wrong example with wrong URL in KDOC`() {
        lintMethod(LineLength(),
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |import org.cqfn.diktat.ruleset.rules.LineLength
                    |import org.cqfn.diktat.util.lintMethod
                    |
                    |/**
                    |*dhttps://www.google.com/search?q=posible+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    |*https://www.google.com/search?q=posible+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    |*@param a
                    |*/
                    |
                    |class A{
                    |   companion object {
                    |   }
                    |
                    |   fun foo() {
                    |   }
                    |}
                """.trimMargin(),
                LintError(7,1,ruleId,"${LONG_LINE.warnText()} dhttps://www.google.com/search?q=posible+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8", false)
        )
    }

    @Test
    fun `check wrong example with wrong URL in KDOC with configuration`() {
        lintMethod(LineLength(),
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |import org.cqfn.diktat.ruleset.rules.LineLength
                    |import org.cqfn.diktat.util.lintMethod
                    |
                    |/**
                    |*dhttps://www.google.com/search?q=posible+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    |*https://www.google.com/search?q=posible+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    |*@param a
                    |*/
                    |
                    |class A{
                    |   companion object {
                    |   }
                    |
                    |   fun foo() {
                    |   }
                    |}
                """.trimMargin(),
                rulesConfigList = rulesConfigListLineLength
        )
    }

    @Test
    fun `check wrong example with long line`() {
        lintMethod(LineLength(),
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |import org.cqfn.diktat.ruleset.rules.LineLength
                    |import org.cqfn.diktat.util.lintMethod
                    |
                    |/**
                    |*https://www.google.com/search?q=posible+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    |*https://www.google.com/search?q=posible+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    |*@param a
                    |*/
                    |
                    |class A{
                    |   companion object {
                    |   }
                    |
                    |   fun foo() {
                    |       val str = "sdjhkjdfhkjsdhfkshfkjshkfhsdkjfhskjdfhkshdfkjsdhfkjsdhfkshdkfhsdkjfhskdjfhkjsdfhkjsdhfjksdhfkjsdhfjkhsdkjfhskdjfhksdfhskdhf"
                    |   }
                    |}
                """.trimMargin(),
                LintError(17,86,ruleId,"${LONG_LINE.warnText()}        val str = \"sdjhkjdfhkjsdhfkshfkjshkfhsdkjfhskjdfhkshdfkjsdhfkjsdhfkshdkfhsdkjfhskdjfhkjsdfhkjsdhfjksdhfkjsdhfjkhsdkjfhskdjfhksdfhskdhf\"", false)
        )
    }

    @Test
    fun `check wrong example with long line but with configuration`() {
        lintMethod(LineLength(),
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |import org.cqfn.diktat.ruleset.rules.LineLength
                    |import org.cqfn.diktat.util.lintMethod
                    |
                    |/**
                    |*https://www.google.com/search?q=posible+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    |*https://www.google.com/search?q=posible+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    |*@param a
                    |*/
                    |
                    |class A{
                    |   companion object {
                    |   }
                    |
                    |   fun foo() {
                    |       val str = "sdjhkjdfhkjsdhfkshfkjshkfhsdkjfhskjdfhkshdfkjsdhfkjsdhfkshdkfhsdkjfhskdjfhkjsdfhkjsdhfjksdhfkjsdhfjkhsdkjfhskdjfhksdfhskdhf"
                    |   }
                    |}
                """.trimMargin(),
                rulesConfigList = rulesConfigListLineLength
        )
    }
}
