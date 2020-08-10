package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.LONG_LINE
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.LineLength
import org.cqfn.diktat.util.lintMethod
import org.junit.Test

class LineLengthWarnTest {

    private val ruleId = "$DIKTAT_RULE_SET_ID:line-length"

    private val rulesConfigListLineLength: List<RulesConfig> = listOf(
            RulesConfig(LONG_LINE.name, true,
                    mapOf("lineLength" to "161"))
    )

    private val wrongURL = "dhttps://www.google.com/search?q=djfhvkdfhvkdh+gthtdj%" +
            "3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome.." +
            "69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8"

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
                    | * https://www.google.com/search?q=djfhvkdfhvkdh+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    | * https://www.google.com/search?q=djfhvkdfhvkdh+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    | * @param a
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
                    | * https://github.com/pinterest/ktlint/blob/master/ktlint-ruleset-standard/src/main/kotlin/com/pinterest/ktlint/ruleset/standard/MaxLineLengthRule.kt
                    | * $wrongURL
                    | * https://www.google.com/search?q=djfhvkdfhvkdh+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    | * @param a
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
                LintError(8,3,ruleId,"${LONG_LINE.warnText()} max line length 120, but was 161", false)
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
                    | * $wrongURL
                    | * https://www.google.com/search?q=djfhvkdfhvkdh+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    | * @param a
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
                    | * https://www.google.com/search?q=djfhvkdfhvkdh+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    | * https://www.google.com/search?q=djfhvkdfhvkdh+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    | * @param a
                    |*/
                    |
                    |class A{
                    |   companion object {
                    |        val str = "sdjhkjdfhkjsdhfkshfkjshkfhsdkjfhskjdfhkshdfkjsdhfkjsdhfkshdkfhsdkjfhskdjfhkjsdfhkjsdhfjksdhfkjsdhfjkhsdkjfhskdjfhksdfhskdhf"
                    |   }
                    |
                    |   fun foo() {
                    |       val str = "sdjhkjdfhkjsdhfkshfkjshkfhsdkjfhskjdfhkshdfkjsdhfkjsdhfkshdkfhsdkjfhskdjfhkjsdfhkjsdhfjksdhfkjsdhfjkhsdkjfhskdjfhksdfhskdhf"
                    |   }
                    |}
                """.trimMargin(),
                LintError(14,1,ruleId,"${LONG_LINE.warnText()} max line length 120, but was 143", false),
                LintError(18,1,ruleId,"${LONG_LINE.warnText()} max line length 120, but was 142", false)
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
                    | * https://www.google.com/search?q=djfhvkdfhvkdh+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    | * https://www.google.com/search?q=djfhvkdfhvkdh+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    | * @param a
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

    @Test
    fun `check correct example with long URL in KDOC in class`() {
        lintMethod(LineLength(),
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |import org.cqfn.diktat.ruleset.rules.LineLength
                    |import org.cqfn.diktat.util.lintMethod
                    |
                    |
                    |class A {
                    |   fun test() {
                    |       /**
                    |       * https://www.google.com/search?q=djfhvkdfhvkdh+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    |       * https://www.google.com/search?q=djfhvkdfhvkdh+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    |       * @param a
                    |       */
                    |       println(123)
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `check wrong examples with long function name and properties`() {
        lintMethod(LineLength(),
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |import org.cqfn.diktat.ruleset.rules.LineLength
                    |import org.cqfn.diktat.util.lintMethod
                    |
                    |
                    |class A {
                    |   fun functionNameisTooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooLong() {
                    |       val text = "sdfkjhsdhjfgdjghdfjghdkfjghdkjghdfkghdkjhfgkjdfhgkjdfhgkjdhgfkjdfhgkjdhgkhdfkghdiulghfdilughdsdcsdcsdcs"
                    |       println("dhfgkjdhfgkjhdkfjghdkjfghdkjfhgkdfhgdkghkghdkjfhgdkghfkdjhfgkjdhfgjkdhfgkjddhfgkdhfgjkdh")
                    |   }
                    |}
                """.trimMargin(),
                LintError(8,1,ruleId,"${LONG_LINE.warnText()} max line length 120, but was 130", false),
                LintError(9,1,ruleId,"${LONG_LINE.warnText()} max line length 120, but was 123", false)
        )
    }
}
