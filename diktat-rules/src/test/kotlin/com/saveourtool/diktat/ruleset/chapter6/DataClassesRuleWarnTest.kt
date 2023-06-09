package com.saveourtool.diktat.ruleset.chapter6

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter6.classes.DataClassesRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames.USE_DATA_CLASS
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class DataClassesRuleWarnTest : LintTestBase(::DataClassesRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${DataClassesRule.NAME_ID}"

    @Test
    @Tag(USE_DATA_CLASS)
    fun `trigger on default class`() {
        lintMethod(
            """
                    |class Some(val a: Int = 5) {
                    |
                    |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${Warnings.USE_DATA_CLASS.warnText()} Some")
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `_regression_ trigger on default class without a body`() {
        lintMethod(
            """
                    |class Some(val a: Int = 5)
                    |
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${Warnings.USE_DATA_CLASS.warnText()} Some")
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should trigger - dont forget to consider this class in fix`() {
        lintMethod(
            """
                    |class Test {
                    |   var a: Int = 0
                    |          get() = field
                    |          set(value: Int) { field = value}
                    |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${Warnings.USE_DATA_CLASS.warnText()} Test")
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should not trigger if there is some logic in accessor`() {
        lintMethod(
            """
                    |class Test {
                    |   var a: Int = 0
                    |          get() = field
                    |          set(value: Int) {
                    |              field = value
                    |              someFun(value)
                    |          }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should not trigger on class with bad modifiers`() {
        lintMethod(
            """
                    |data class Some(val a: Int = 5) {
                    |
                    |}
                    |
                    |abstract class Another() {}
                    |
                    |open class Open(){}
                    |
                    |sealed class Clazz{}
                    |
                    |data class CheckInner {
                    |   inner class Inner {}
                    |}
                    |
                    |enum class Num {
                    |
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should not trigger on classes with functions`() {
        lintMethod(
            """
                    |class Some {
                    |   val prop = 5
                    |   private fun someFunc() {}
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should not trigger on classes with no property in constructor`() {
        lintMethod(
            """
                    |class B(map: Map<Int,Int>) {}
                    |
                    |class Ab(val map: Map<Int, Int>, map: Map<Int, Int>) {}
                    |
                    |class A(val map: Map<Int, Int>) {}
                    |
            """.trimMargin(),
            DiktatError(5, 1, ruleId, "${Warnings.USE_DATA_CLASS.warnText()} A")
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should not trigger on empty class`() {
        lintMethod(
            """
                    |class B() {}
                    |
                    |class Ab{}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should trigger on class without constructor but with property`() {
        lintMethod(
            """
                    |class B() {
                    |   val q = 10
                    |}
                    |
                    |class Ab {
                    |   val qw = 10
                    |}
                    |
                    |class Ba {
                    |   val q = 10
                    |   fun foo() = 10
                    |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${Warnings.USE_DATA_CLASS.warnText()} B"),
            DiktatError(5, 1, ruleId, "${Warnings.USE_DATA_CLASS.warnText()} Ab")
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `shouldn't trigger on class with init block`() {
        lintMethod(
            """
                |class Credentials(auth: String) {
                |   val gitHubUserName: String
                |   val gitHubAuthToken: String
                |
                |   init {
                |       auth.let {
                |
                |       }
                |   }
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should not trigger on init block with if`() {
        lintMethod(
            """
                |class Credentials(auth: String, second: Int?, third: Double) {
                |   val gitHubUserName: String
                |   val gitHubAuthToken: String
                |
                |   init {
                |       if (second != null) {
                |       }
                |   }
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should not trigger on init block with function call`() {
        lintMethod(
            """
                |class Credentials(auth: String, second: Int?, third: Double) {
                |   val gitHubUserName: String
                |   val gitHubAuthToken: String
                |
                |   init {
                |       foo(third)
                |   }
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should not trigger on class with several parameters`() {
        lintMethod(
            """
                |class Credentials(auth: String, second: Int?, third: Double) {
                |   val gitHubUserName: String
                |   val gitHubAuthToken: String
                |
                |   init {
                |       auth.let {
                |
                |       }
                |
                |       if (second != null) {
                |       }
                |
                |       foo(third)
                |   }
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should not trigger on enums`() {
        lintMethod(
            """
                |enum class Style(val str: String) {
                |    PASCAL_CASE("PascalCase"),
                |    SNAKE_CASE("UPPER_SNAKE_CASE"),
                |    ;
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should trigger on class with parameter in constructor`() {
        lintMethod(
            """
                |class Credentials(auth: String) {
                |   val gitHubUserName: String = auth.toUpperCase()
                |   val gitHubAuthToken: String = auth.toLowerCase()
                |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${Warnings.USE_DATA_CLASS.warnText()} Credentials")
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should trigger on class with parameter in constructor and init block`() {
        lintMethod(
            """
                |class Credentials(auth: String) {
                |   val gitHubUserName: String = auth.toUpperCase()
                |   val gitHubAuthToken: String = auth.toLowerCase()
                |
                |   init {
                |       // some logic
                |   }
                |}
            """.trimMargin(),
            DiktatError(1, 1, ruleId, "${Warnings.USE_DATA_CLASS.warnText()} Credentials")
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `should not trigger on init block with one ref expression`() {
        lintMethod(
            """
                |class Credentials(auth: String, some: Int?) {
                |   val gitHubUserName: String = auth.toUpperCase()
                |   val gitHubAuthToken: String = auth.toLowerCase()
                |
                |   init {
                |       val a = auth
                |   }
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `annotation classes bug`() {
        lintMethod(
            """
                |@Retention(AnnotationRetention.SOURCE)
                |@Target(AnnotationTarget.CLASS)
                |annotation class NavGraphDestination(
                |    val name: String = Defaults.NULL,
                |    val routePrefix: String = Defaults.NULL,
                |    val deepLink: Boolean = false,
                |) {
                |    object Defaults {
                |        const val NULL = "@null"
                |    }
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `value or inline classes bug`() {
        lintMethod(
            """
                |@JvmInline
                |value class Password(private val s: String)
                |val securePassword = Password("Don't try this in production")
            """.trimMargin()
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `sealed classes bug`() {
        lintMethod(
            """
                |sealed class Password(private val s: String)
            """.trimMargin()
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `inner classes bug`() {
        lintMethod(
            """
                |inner class Password(private val s: String)
            """.trimMargin()
        )
    }

    @Test
    @Tag(USE_DATA_CLASS)
    fun `shouldn't trigger on interface`() {
        lintMethod(
            """
                |interface Credentials {
                |   val code: String
                |   val success: Boolean
                |   val message: String
                |}
            """.trimMargin()
        )
    }
}
