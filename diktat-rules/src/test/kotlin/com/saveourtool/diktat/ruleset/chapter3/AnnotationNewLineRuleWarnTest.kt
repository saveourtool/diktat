package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter3.AnnotationNewLineRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class AnnotationNewLineRuleWarnTest : LintTestBase(::AnnotationNewLineRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${AnnotationNewLineRule.NAME_ID}"

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation class test good`() {
        lintMethod(
            """
                    |@SomeAnnotation
                    |@SecondAnnotation
                    |class A {
                    |   val a = 5
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation class test good 2`() {
        lintMethod(
            """
                    |@SomeAnnotation class A {
                    |   val a = 5
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation class test bad`() {
        lintMethod(
            """
                    |@SomeAnnotation @SecondAnnotation
                    |class A {
                    |   val a = 5
                    |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SomeAnnotation not on a single line", true),
            DiktatError(1, 17, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SecondAnnotation not on a single line", true)
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation class test bad 2`() {
        lintMethod(
            """
                    |@SomeAnnotation @SecondAnnotation class A {
                    |   val a = 5
                    |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SomeAnnotation not on a single line", true),
            DiktatError(1, 17, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SecondAnnotation not on a single line", true)
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation fun test good`() {
        lintMethod(
            """
                    |class A {
                    |   val a = 5
                    |
                    |  @SomeAnnotation
                    |  @SecondAnnotation
                    |  fun someFunc() {
                    |       val a = 3
                    |  }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation fun test good 2`() {
        lintMethod(
            """
                    |class A {
                    |   val a = 5
                    |
                    |  @SomeAnnotation fun someFunc() {
                    |       val a = 3
                    |  }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation fun test bad`() {
        lintMethod(
            """
                    |class A {
                    |   val a = 5
                    |
                    |  @SomeAnnotation @SecondAnnotation fun someFunc() {
                    |       val a = 3
                    |  }
                    |}
            """.trimMargin(),
            DiktatError(4, 3, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SomeAnnotation not on a single line", true),
            DiktatError(4, 19, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SecondAnnotation not on a single line", true)
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation fun test bad 2`() {
        lintMethod(
            """
                    |class A {
                    |   val a = 5
                    |
                    |  @SomeAnnotation @SecondAnnotation
                    |  fun someFunc() {
                    |       val a = 3
                    |  }
                    |}
            """.trimMargin(),
            DiktatError(4, 3, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SomeAnnotation not on a single line", true),
            DiktatError(4, 19, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SecondAnnotation not on a single line", true)
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation constructor test good`() {
        lintMethod(
            """
                    |public class Conf
                    |@Inject
                    |constructor(conf: Int) {
                    |
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation constructor test good 2`() {
        lintMethod(
            """
                    |public class Conf @Inject constructor(conf: Int) {
                    |
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation constructor test good 3`() {
        lintMethod(
            """
                    |public class Conf
                    |@Inject
                    |@SomeAnnotation
                    |constructor(conf: Int) {
                    |
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation secondary constructor test good`() {
        lintMethod(
            """
                    |public class Conf {
                    |   @FirstAnnotation constructor(conf: Conf) {
                    |
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation secondary constructor test bad`() {
        lintMethod(
            """
                    |public class Conf {
                    |   @FirstAnnotation @SecondAnnotation constructor(conf: Conf) {
                    |
                    |   }
                    |}
            """.trimMargin(),
            DiktatError(2, 4, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @FirstAnnotation not on a single line", true),
            DiktatError(2, 21, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SecondAnnotation not on a single line", true)
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation constructor test bad`() {
        lintMethod(
            """
                    |public class Conf @Inject @SomeAnnotation constructor(conf: Int) {
                    |
                    |}
            """.trimMargin(),
            DiktatError(1, 19, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @Inject not on a single line", true),
            DiktatError(1, 27, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SomeAnnotation not on a single line", true)
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation constructor test bad 2`() {
        lintMethod(
            """
                    |public class Conf @Inject
                    |@SomeAnnotation constructor(conf: Int) {
                    |
                    |}
            """.trimMargin(),
            DiktatError(1, 19, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @Inject not on a single line", true),
            DiktatError(2, 1, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SomeAnnotation not on a single line", true)
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `no warns in func params`() {
        lintMethod(
            """
                    |public class Conf {
                    |   fun someFunc(@SomeAnnotation conf: JsonConf, @SecondAnnotation some: Int) {
                    |
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `no warns in func params 2`() {
        lintMethod(
            """
                    |public class Conf {
                    |   fun someFunc(@SomeAnnotation @AnotherAnnotation conf: JsonConf, @SecondAnnotation @ThirdAnnotation some: Int) {
                    |
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `no warn in correct annotation with comment`() {
        lintMethod(
            """
                    |@ExperimentalStdlibApi  // to use `scan` on sequence
                    |   @Suppress("WRONG_NEWLINES")
                    |   override fun checkNode() {}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `should warn annotation for several annotations`() {
        lintMethod(
            """
                    |@ExperimentalStdlibApi /*   */ @Hello
                    |override fun checkNode() {}
                    |
                    |/*    */ @Goo
                    |class A {}
                    |
                    |@A1
                    |/*   */ @A2
                    |@A3
                    |class A {}
                    |
                    |
                    |@Foo class Foo {}
                    |
                    |@Foo
                    |class Foo {}
                    |
                    |@Foo @Goo val loader: DataLoader
                    |
                    |@Foo
                    |@goo val loader: DataLoader
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @ExperimentalStdlibApi not on a single line", true),
            DiktatError(1, 32, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @Hello not on a single line", true),
        )
    }
}
