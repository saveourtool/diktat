@file:Suppress("LONG_LINE")

package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.LONG_LINE
import com.saveourtool.diktat.ruleset.rules.chapter3.LineLength
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class LineLengthWarnTest : LintTestBase(::LineLength) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${LineLength.NAME_ID}"
    private val rulesConfigListLineLength: List<RulesConfig> = listOf(
        RulesConfig(LONG_LINE.name, true,
            mapOf("lineLength" to "163"))
    )
    private val shortLineLength: List<RulesConfig> = listOf(
        RulesConfig(LONG_LINE.name, true,
            mapOf("lineLength" to "40"))
    )
    private val wrongUrl = "dhttps://www.google.com/search?q=djfhvkdfhvkdh+gthtdj%" +
            "3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome.." +
            "69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8"
    private val correctUrl = "https://www.google.com/search?q=djfhvkdfhvkdh+gthtdj%" +
            "3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome.." +
            "69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8"

    @Test
    @Tag(WarningNames.LONG_LINE)
    fun `check correct example with long URL in KDOC and long import`() {
        lintMethod(
            """
                    |package com.saveourtool.diktat.ruleset.chapter3
                    |
                    |import com.saveourtool.diktat.ruleset.rules.LineLength.sdfsdfsf.sdfsdfsdfsdfdghdf.gfhdf.hdstst.dh.dsgfdfgdgs.rhftheryryj.cgh
                    |import com.saveourtool.diktat.util.lintMethod
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
            """.trimMargin(),
            rulesConfigList = shortLineLength
        )
    }

    @Test
    @Tag(WarningNames.LONG_LINE)
    fun `check wrong example with wrong URL in KDOC`() {
        lintMethod(
            """
                    |package com.saveourtool.diktat.ruleset.chapter3
                    |
                    |import com.saveourtool.diktat.ruleset.rules.chapter3.LineLength
                    |import com.saveourtool.diktat.util.lintMethod
                    |
                    |/**
                    | * https://github.com/pinterest/ktlint/blob/master/ktlint-ruleset-standard/src/main/kotlin/com/pinterest/ktlint/ruleset/standard/MaxLineLengthRule.kt
                    | * $wrongUrl
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
            DiktatError(8, 1, ruleId, "${LONG_LINE.warnText()} max line length 120, but was 163", false)
        )
    }

    @Test
    @Tag(WarningNames.LONG_LINE)
    fun `check wrong example with wrong URL in KDOC with configuration`() {
        lintMethod(
            """
                    |package com.saveourtool.diktat.ruleset.chapter3
                    |
                    |import com.saveourtool.diktat.ruleset.rules.chapter3.LineLength
                    |import com.saveourtool.diktat.util.lintMethod
                    |
                    |/**
                    | * $wrongUrl
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
    @Tag(WarningNames.LONG_LINE)
    fun `check wrong example with long line`() {
        lintMethod(
            """
                    |package com.saveourtool.diktat.ruleset.chapter3
                    |
                    |import com.saveourtool.diktat.ruleset.rules.chapter3.LineLength
                    |import com.saveourtool.diktat.util.lintMethod
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
            DiktatError(14, 1, ruleId, "${LONG_LINE.warnText()} max line length 120, but was 143", true),
            DiktatError(18, 1, ruleId, "${LONG_LINE.warnText()} max line length 120, but was 142", true)
        )
    }

    @Test
    @Tag(WarningNames.LONG_LINE)
    fun `check wrong example with long line but with configuration`() {
        lintMethod(
            """
                    |package com.saveourtool.diktat.ruleset.chapter3
                    |
                    |import com.saveourtool.diktat.ruleset.rules.chapter3.LineLength
                    |import com.saveourtool.diktat.util.lintMethod
                    |
                    |/**
                    | * This is very important URL https://www.google.com/search?q=djfhvkdfhvkdh+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    | * https://www.google.com/search?q=djfhvkdfhvkdh+gthtdj%3Bb&rlz=1C1GCEU_enRU909RU909&oq=posible+gthtdj%3Bb&aqs=chrome..69i57j0l3.2680j1j7&sourceid=chrome&ie=UTF-8
                    | * $correctUrl this text can be on another line
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
            DiktatError(9, 1, ruleId, "${LONG_LINE.warnText()} max line length 163, but was 195", false),
            rulesConfigList = rulesConfigListLineLength
        )
    }

    @Test
    @Tag(WarningNames.LONG_LINE)
    fun `check correct example with long URL in KDOC in class`() {
        lintMethod(
            """
                    |package com.saveourtool.diktat.ruleset.chapter3
                    |
                    |import com.saveourtool.diktat.ruleset.rules.chapter3.LineLength
                    |import com.saveourtool.diktat.util.lintMethod
                    |
                    |
                    |class A {
                    |   fun test() {
                    |       /**
                    |       * [link]($correctUrl)
                    |       * [link]$correctUrl
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
    @Tag(WarningNames.LONG_LINE)
    fun `check wrong examples with long function name and properties`() {
        lintMethod(
            """
                    |package com.saveourtool.diktat.ruleset.chapter3
                    |
                    |import com.saveourtool.diktat.ruleset.rules.chapter3.LineLength
                    |import com.saveourtool.diktat.util.lintMethod
                    |
                    |
                    |class A {
                    |   fun functionNameisTooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooLong() {
                    |       val text = "sdfkjhsdhjfgdjghdfjghdkfjghdkjghdfkghdkjhfgkjdfhgkjdfhgkjdhgfkjdfhgkjdhgkhdfkghdiulghfdilughdsdcsdcsdcs"
                    |       println("dhfgkjdhfgkjhdkfjghdkjfghdkjfhgkdfhgdkghkghdkjfhgdkghfkdjhfgkjdhfgjkdhfgkjddhfgkdhfgjkdh")
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(8, 1, ruleId, "${LONG_LINE.warnText()} max line length 120, but was 130", false),
            DiktatError(9, 1, ruleId, "${LONG_LINE.warnText()} max line length 120, but was 123", true)

        )
    }

    @Test
    @Tag(WarningNames.LONG_LINE)
    fun `check annotation and fun with expr body`() {
        lintMethod(
            """
                    |@Query(value = "ASDAASDASDASDASDASDASDASDAASDASDASDASDASDASDASDAASDASDASDASDASDASD")
                    |fun foo() = println("ASDAASDASDASDASDASDASDASDAASDASDASDASDASDASDASDAASDASDASDASDASDASD")
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${LONG_LINE.warnText()} max line length 40, but was 84", true),
            DiktatError(2, 1, ruleId, "${LONG_LINE.warnText()} max line length 40, but was 89", true),
            rulesConfigList = shortLineLength
        )
    }
}
