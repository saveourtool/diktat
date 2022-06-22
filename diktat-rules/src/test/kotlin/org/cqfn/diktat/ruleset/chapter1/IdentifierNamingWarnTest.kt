package org.cqfn.diktat.ruleset.chapter1

import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.BACKTICKS_PROHIBITED
import org.cqfn.diktat.ruleset.constants.Warnings.CLASS_NAME_INCORRECT
import org.cqfn.diktat.ruleset.constants.Warnings.CONFUSING_IDENTIFIER_NAMING
import org.cqfn.diktat.ruleset.constants.Warnings.CONSTANT_UPPERCASE
import org.cqfn.diktat.ruleset.constants.Warnings.ENUM_VALUE
import org.cqfn.diktat.ruleset.constants.Warnings.EXCEPTION_SUFFIX
import org.cqfn.diktat.ruleset.constants.Warnings.FUNCTION_BOOLEAN_PREFIX
import org.cqfn.diktat.ruleset.constants.Warnings.GENERIC_NAME
import org.cqfn.diktat.ruleset.constants.Warnings.IDENTIFIER_LENGTH
import org.cqfn.diktat.ruleset.constants.Warnings.OBJECT_NAME_INCORRECT
import org.cqfn.diktat.ruleset.constants.Warnings.VARIABLE_HAS_PREFIX
import org.cqfn.diktat.ruleset.constants.Warnings.VARIABLE_NAME_INCORRECT
import org.cqfn.diktat.ruleset.constants.Warnings.VARIABLE_NAME_INCORRECT_FORMAT
import org.cqfn.diktat.ruleset.rules.chapter1.IdentifierNaming
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test

class IdentifierNamingWarnTest : LintTestBase(::IdentifierNaming) {
    private val ruleId: String = "$DIKTAT_RULE_SET_ID:${IdentifierNaming.NAME_ID}"
    private val rulesConfigBooleanFunctions: List<RulesConfig> = listOf(
        RulesConfig(FUNCTION_BOOLEAN_PREFIX.name, true,
            mapOf("allowedPrefixes" to "equals, equivalent, foo"))
    )

    // ======== checks for generics ========
    @Test
    @Tag(WarningNames.GENERIC_NAME)
    fun `generic class - single capital letter, can be followed by a number  (check - positive1)`() {
        val code =
            """
                package org.cqfn.diktat.test

                class TreeNode<T>(val value: T?, val next: TreeNode<T>? = null)

            """.trimIndent()
        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.GENERIC_NAME)
    fun `generic class - single capital letter, can be followed by a number  (check - positive2)`() {
        val code =
            """
                package org.cqfn.diktat.test

                class TreeNode<T123>(val value: T?, val next: TreeNode<T>? = null)

            """.trimIndent()
        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.GENERIC_NAME)
    fun `anonymous function`() {
        val code = """
            package org.cqfn.diktat.test

            fun foo() {
                val sum: (Int) -> Int = fun(x): Int = x + x
            }

        """.trimIndent()
        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.GENERIC_NAME)
    fun `generic class - single capital letter, can be followed by a number  (check - negative1)`() {
        val code =
            """
                package org.cqfn.diktat.test

                class TreeNode<a>(val value: T?, val next: TreeNode<T>? = null)

            """.trimIndent()
        lintMethod(code, LintError(
            3, 15, ruleId, "${GENERIC_NAME.warnText()} <a>", true)
        )
    }

    @Test
    @Tag(WarningNames.GENERIC_NAME)
    fun `generic class - single capital letter, can be followed by a number  (check - negative2)`() {
        val code =
            """
                package org.cqfn.diktat.test

                class TreeNode<TBBB>(val value: T?, val next: TreeNode<T>? = null)

            """.trimIndent()
        lintMethod(code, LintError(
            3, 15, ruleId, "${GENERIC_NAME.warnText()} <TBBB>", true)
        )
    }

    @Test
    @Tag(WarningNames.GENERIC_NAME)
    fun `generic method - single capital letter, can be followed by a number  (check)`() {
        val code =
            """
                package org.cqfn.diktat.test

                fun <T> makeLinkedList(vararg elements: T): TreeNode<T>? {
                    var node: TreeNode<T>? = null
                    for (element in elements.reversed()) {
                         node = TreeNode(element, node)
                    }
                    return node
                }
            """.trimIndent()
        lintMethod(code)
    }

    // ======== checks for variables and class names ========
    @Test
    @Tag(WarningNames.CLASS_NAME_INCORRECT)
    fun `check class name (check)`() {
        val code =
            """
                class incorrectNAME {}
                class IncorrectNAME {}
            """
        lintMethod(code,
            LintError(2, 23, ruleId, "${CLASS_NAME_INCORRECT.warnText()} incorrectNAME", true),
            LintError(3, 23, ruleId, "${CLASS_NAME_INCORRECT.warnText()} IncorrectNAME", true)
        )
    }

    @Test
    @Tags(
        Tag(WarningNames.CLASS_NAME_INCORRECT),
        Tag(WarningNames.VARIABLE_NAME_INCORRECT_FORMAT),
        Tag(WarningNames.CONSTANT_UPPERCASE)
    )
    fun `check identifiers case format (check - negative)`() {
        val code =
            """
              var SOMEtest = "TEST"
              const val thisConstantShouldBeUpper = "CONST"
              class className {
                  data class badClassName(val FIRST: String, var SECOND: String)

                  companion object {
                      const val incorrect_case = ""
                      val correctCase = ""
                      var INCORRECT = ""
                  }

                  var check_me = ""
                  val CHECK_ME = ""
              }
            """.trimIndent()

        lintMethod(code,
            LintError(1, 5, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} SOMEtest", true),
            LintError(2, 11, ruleId, "${CONSTANT_UPPERCASE.warnText()} thisConstantShouldBeUpper", true),
            LintError(3, 7, ruleId, "${CLASS_NAME_INCORRECT.warnText()} className", true),
            LintError(4, 16, ruleId, "${CLASS_NAME_INCORRECT.warnText()} badClassName", true),
            LintError(4, 33, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} FIRST", true),
            LintError(4, 52, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} SECOND", true),
            LintError(7, 19, ruleId, "${CONSTANT_UPPERCASE.warnText()} incorrect_case", true),
            LintError(9, 13, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} INCORRECT", true),
            LintError(12, 9, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} check_me", true),
            LintError(13, 9, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} CHECK_ME", true)
        )
    }

    @Test
    @Tags(Tag(WarningNames.IDENTIFIER_LENGTH), Tag(WarningNames.VARIABLE_NAME_INCORRECT))
    fun `check variable length (check - negative)`() {
        val code =
            """
                val r = 0
                val x256 = 256
                val i = 1
                class LongLongLongLongLongLongLongLongLongLongLongLongLongLongLongLongLongName {
                    val veryLongveryLongveryLongveryLongveryLongveryLongveryLongveryLongveryLongName =                "                "
                }
            """.trimIndent()
        lintMethod(code,
            LintError(1, 5, ruleId, "${IDENTIFIER_LENGTH.warnText()} r"),
            LintError(2, 5, ruleId, "${VARIABLE_NAME_INCORRECT.warnText()} x256"),
            LintError(4, 7, ruleId, "${IDENTIFIER_LENGTH.warnText()} LongLongLongLongLongLongLongLongLongLongLongLongLongLongLongLongLongName"),
            LintError(5, 9, ruleId, "${IDENTIFIER_LENGTH.warnText()} veryLongveryLongveryLongveryLongveryLongveryLongveryLongveryLongveryLongName")
        )
    }

    @Test
    @Tag(WarningNames.VARIABLE_NAME_INCORRECT_FORMAT)
    fun `check value parameters in dataclasses (check - negative)`() {
        val code =
            """
                data class ClassName(val FIRST: String, var SECOND: String)
            """.trimIndent()
        lintMethod(code,
            LintError(1, 26, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} FIRST", true),
            LintError(1, 45, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} SECOND", true)
        )
    }

    @Test
    @Tag(WarningNames.VARIABLE_NAME_INCORRECT_FORMAT)
    fun `check value parameters in functions (check - negative)`() {
        val code =
            """
                fun foo(SOMENAME: String) {
                }
            """.trimIndent()
        lintMethod(code,
            LintError(1, 9, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} SOMENAME", true)
        )
    }

    @Test
    @Tags(Tag(WarningNames.ENUM_VALUE), Tag(WarningNames.CLASS_NAME_INCORRECT))
    fun `check case for enum values (check - negative)`() {
        val code =
            """
                enum class TEST_ONE {
                    first_value, secondValue, thirdVALUE, FourthValue
                }
            """.trimIndent()
        lintMethod(code,
            LintError(1, 12, ruleId, "${CLASS_NAME_INCORRECT.warnText()} TEST_ONE", true),
            LintError(2, 5, ruleId, "${ENUM_VALUE.warnText()} first_value", true),
            LintError(2, 18, ruleId, "${ENUM_VALUE.warnText()} secondValue", true),
            LintError(2, 31, ruleId, "${ENUM_VALUE.warnText()} thirdVALUE", true),
            LintError(2, 43, ruleId, "${ENUM_VALUE.warnText()} FourthValue", true)
        )
    }

    @Test
    @Tags(Tag(WarningNames.ENUM_VALUE), Tag(WarningNames.CLASS_NAME_INCORRECT))
    fun `check case for pascal case enum values (check - negative)`() {
        val rulesConfigPascalCaseEnum: List<RulesConfig> = listOf(
            RulesConfig(ENUM_VALUE.name, true,
                mapOf("enumStyle" to "pascalCase"))
        )
        val code =
            """
                enum class TEST_ONE {
                    first_value, secondValue, thirdVALUE, FOURTH_VALUE
                }
            """.trimIndent()
        lintMethod(code,
            LintError(1, 12, ruleId, "${CLASS_NAME_INCORRECT.warnText()} TEST_ONE", true),
            LintError(2, 5, ruleId, "${ENUM_VALUE.warnText()} first_value", true),
            LintError(2, 18, ruleId, "${ENUM_VALUE.warnText()} secondValue", true),
            LintError(2, 31, ruleId, "${ENUM_VALUE.warnText()} thirdVALUE", true),
            LintError(2, 43, ruleId, "${ENUM_VALUE.warnText()} FOURTH_VALUE", true),
            rulesConfigList = rulesConfigPascalCaseEnum
        )
    }

    @Test
    @Tag(WarningNames.OBJECT_NAME_INCORRECT)
    fun `check case for object (check - negative)`() {
        val code =
            """
                object TEST_ONE {
                }
            """.trimIndent()
        lintMethod(code,
            LintError(1, 8, ruleId, "${OBJECT_NAME_INCORRECT.warnText()} TEST_ONE", true)
        )
    }

    // ======== exception case and suffix ========
    @Test
    @Tag(WarningNames.CLASS_NAME_INCORRECT)
    fun `check exception case format`() {
        val code =
            """
                class incorrect_case_Exception(message: String) : Exception(message)
            """.trimIndent()
        lintMethod(code,
            LintError(1, 7, ruleId, "${CLASS_NAME_INCORRECT.warnText()} incorrect_case_Exception", true)
        )
    }

    @Test
    @Tag(WarningNames.EXCEPTION_SUFFIX)
    fun `check exception case and suffix (with type call entry) - negative`() {
        val code =
            """
                class Custom(message: String) : Exception(message)
            """.trimIndent()
        lintMethod(code,
            LintError(1, 7, ruleId, "${EXCEPTION_SUFFIX.warnText()} Custom", true)
        )
    }

    @Test
    @Tag(WarningNames.EXCEPTION_SUFFIX)
    fun `check exception case and suffix (only parent name inheritance) - negative`() {
        val code =
            """
                class Custom: Exception {
                    constructor(msg: String) : super(msg)
                }
            """.trimIndent()
        lintMethod(code,
            LintError(1, 7, ruleId, "${EXCEPTION_SUFFIX.warnText()} Custom", true)
        )
    }

    @Test
    @Tag(WarningNames.VARIABLE_HAS_PREFIX)
    fun `checking that there should be no prefixes in variable name`() {
        val code =
            """
                const val M_GLOB = ""
                val aPrefix = ""
            """.trimIndent()
        lintMethod(code,
            LintError(1, 11, ruleId, "${VARIABLE_HAS_PREFIX.warnText()} M_GLOB", true),
            LintError(2, 5, ruleId, "${VARIABLE_HAS_PREFIX.warnText()} aPrefix", true)
        )
    }

    @Test
    @Tags(Tag(WarningNames.VARIABLE_NAME_INCORRECT_FORMAT), Tag(WarningNames.VARIABLE_HAS_PREFIX))
    fun `regression - checking that digit in the end will not raise a warning`() {
        val code =
            """
                val I_AM_CONSTANT1  = ""
                const val I_AM_CONSTANT2  = ""
            """.trimIndent()
        lintMethod(code,
            LintError(1, 5, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} I_AM_CONSTANT1", true),
            LintError(1, 5, ruleId, "${VARIABLE_HAS_PREFIX.warnText()} I_AM_CONSTANT1", true),
            LintError(2, 11, ruleId, "${VARIABLE_HAS_PREFIX.warnText()} I_AM_CONSTANT2", true)
        )
    }

    @Test
    @Tag(WarningNames.VARIABLE_NAME_INCORRECT_FORMAT)
    fun `regression - destructing declaration in lambdas - incorrect case `() {
        val code =
            """
                private fun checkCommentedCode(node: ASTNode) {
                    val eolCommentsOffsetToText = ""
                    val blockCommentsOffsetToText = ""
                    (eolCommentsOffsetToText + blockCommentsOffsetToText)
                    .map { (STRANGECASE, text) ->
                        ""
                    }
                }
            """.trimIndent()
        lintMethod(code,
            LintError(5, 13, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} STRANGECASE", true)
        )
    }

    @Test
    @Tag(WarningNames.VARIABLE_NAME_INCORRECT_FORMAT)
    fun `regression - lambda argument - incorrect case`() {
        val code =
            """
                private fun checkCommentedCode(node: ASTNode) {
                    val eolCommentsOffsetToText = ""
                    eolCommentsOffsetToText.map { STRANGECASE ->
                        ""
                    }
                }
            """.trimIndent()
        lintMethod(code,
            LintError(3, 35, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} STRANGECASE", true)
        )
    }

    @Test
    @Tag(WarningNames.FUNCTION_BOOLEAN_PREFIX)
    fun `FUNCTION_BOOLEAN_PREFIX - positive example`() {
        lintMethod(
            """
                    fun ASTNode.hasEmptyLineAfter(): Boolean { }
                    fun hasEmptyLineAfter(): Boolean { }
                    fun ASTNode.isEmpty(): Boolean { }
                    fun isEmpty(): Boolean { }
                    override fun empty(): Boolean { }
                    override fun ASTNode.empty(): Boolean { }
            """.trimIndent()
        )
    }

    @Test
    @Tag(WarningNames.FUNCTION_BOOLEAN_PREFIX)
    fun `FUNCTION_BOOLEAN_PREFIX - negative example`() {
        lintMethod(
            """
                    fun ASTNode.emptyLineAfter(): Boolean { }
                    fun emptyLineAfter(): Boolean { }
                    fun ASTNode.empty(): Boolean { }
                    fun empty(): Boolean { }
            """.trimIndent(),
            LintError(1, 13, ruleId, "${FUNCTION_BOOLEAN_PREFIX.warnText()} emptyLineAfter", true),
            LintError(2, 5, ruleId, "${FUNCTION_BOOLEAN_PREFIX.warnText()} emptyLineAfter", true),
            LintError(3, 13, ruleId, "${FUNCTION_BOOLEAN_PREFIX.warnText()} empty", true),
            LintError(4, 5, ruleId, "${FUNCTION_BOOLEAN_PREFIX.warnText()} empty", true)
        )
    }

    @Test
    @Tag(WarningNames.FUNCTION_BOOLEAN_PREFIX)
    fun `all prefixes for boolean methods`() {
        lintMethod(
            """
                    fun hasEmptyLineAfter(): Boolean { }
                    fun haveEmptyLineAfter(): Boolean { }
                    fun isEmpty(): Boolean { }
                    fun shouldBeEmpty(): Boolean { }
                    fun areEmpty(): Boolean { }
            """.trimIndent()
        )
    }

    @Test
    @Tag(WarningNames.FUNCTION_BOOLEAN_PREFIX)
    fun `test allowed boolean functions in configuration`() {
        lintMethod(
            """
                    fun equalsSome(): Boolean { }
                    fun fooBar(): Boolean { }
                    fun equivalentToAnother(): Boolean { }
            """.trimIndent(),
            rulesConfigList = rulesConfigBooleanFunctions
        )
    }

    @Test
    @Tag(WarningNames.IDENTIFIER_LENGTH)
    fun `regression - function argument type`() {
        // valid example, should not cause exceptions
        lintMethod(
            """
                    fun foo(predicate: (Int) -> Boolean) = Unit
            """.trimIndent()
        )

        // identifier names in function types are still checked if present
        lintMethod(
            """
                    fun foo(predicate: (a: Int) -> Boolean) = Unit
            """.trimIndent(),
            LintError(1, 21, ruleId, "${IDENTIFIER_LENGTH.warnText()} a", false)
        )
    }

    @Test
    @Tag(WarningNames.IDENTIFIER_LENGTH)
    fun `regression - object parsing should not fail with anonymous objects`() {
        val code =
            """
                val fakeVal = RuleSet("test", object : Rule("astnode-utils-test") {
                                override fun visit(node: ASTNode) {}
                                })
            """.trimIndent()
        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.IDENTIFIER_LENGTH)
    fun `exception case for identifier naming in catch statements`() {
        val code =
            """
                fun foo() {
                    try {
                    } catch (e: IOException) {
                    }
                }
            """.trimIndent()
        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.IDENTIFIER_LENGTH)
    fun `exception case for identifier naming in catch statements with catch body`() {
        val code =
            """
                fun foo() {
                    try {
                    } catch (e: IOException) {
                        fun foo(e: Int) {
                        }
                    }
                }
            """.trimIndent()
        lintMethod(code, LintError(4, 17, ruleId, "${IDENTIFIER_LENGTH.warnText()} e", false))
    }

    @Test
    @Tag(WarningNames.IDENTIFIER_LENGTH)
    fun `exception case for identifier naming - catching exception with type e`() {
        val code =
            """
                fun foo() {
                    try {
                    } catch (e: e) {
                    }
                }
            """.trimIndent()
        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.BACKTICKS_PROHIBITED)
    fun `backticks should be used only with functions from tests (function)`() {
        val code =
            """
                fun `foo function`(`argument with backstick`: String) {
                    val `foo variable` = ""
                }
            """.trimIndent()
        lintMethod(code,
            LintError(1, 5, ruleId, "${BACKTICKS_PROHIBITED.warnText()} `foo function`"),
            LintError(1, 20, ruleId, "${BACKTICKS_PROHIBITED.warnText()} `argument with backstick`"),
            LintError(2, 9, ruleId, "${BACKTICKS_PROHIBITED.warnText()} `foo variable`")
        )
    }

    @Test
    @Tag(WarningNames.BACKTICKS_PROHIBITED)
    fun `backticks should be used only with functions from tests (test method)`() {
        val code =
            """
                @Test
                fun `test function with backstick`() {
                }
            """.trimIndent()
        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.BACKTICKS_PROHIBITED)
    fun `backticks should be used only with functions from tests (test method with variables)`() {
        val code =
            """
                @Test
                fun `test function with backstick`() {
                    val `should not be used` = ""

                }
            """.trimIndent()
        lintMethod(code, LintError(3, 9, ruleId, "${BACKTICKS_PROHIBITED.warnText()} `should not be used`"))
    }

    @Test
    @Tag(WarningNames.BACKTICKS_PROHIBITED)
    fun `backticks should be used only with functions from tests (class)`() {
        val code =
            """
                class `my class name` {}
            """.trimIndent()
        lintMethod(code, LintError(1, 7, ruleId, "${BACKTICKS_PROHIBITED.warnText()} `my class name`"))
    }

    @Test
    @Tag(WarningNames.BACKTICKS_PROHIBITED)
    fun `regression - backticks should be forbidden only in declarations`() {
        lintMethod(
            """
                |fun foo() {
                |    it.assertThat(actual.detail).`as`("Detailed message").isEqualTo(expected.detail)
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.VARIABLE_NAME_INCORRECT_FORMAT)
    fun `should not trigger on underscore`() {
        val code =
            """
                class SomeClass {
                    fun bar() {
                        val ast = list.map {(first, _) -> foo(first)}

                        var meanValue: Int = 0
                            for ((
                                _,
                                _,
                                year
                            ) in cars) {
                                meanValue += year
                            }

                        try {
                            /* ... */
                        } catch (_: IOException) {
                            /* ... */
                        }
                    }
                }
            """.trimIndent()
        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.CONFUSING_IDENTIFIER_NAMING)
    fun `confusing identifier naming`() {
        val code =
            """
                fun someFunc() {
                    val D = 0
                    val Z = 2
                }

                enum class Ident {
                    B
                }
            """.trimIndent()
        lintMethod(code,
            LintError(2, 9, ruleId, "${CONFUSING_IDENTIFIER_NAMING.warnText()} better name is: obj, dgt", false),
            LintError(2, 9, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} D", true),
            LintError(2, 9, ruleId, "${IDENTIFIER_LENGTH.warnText()} D", false),
            LintError(3, 9, ruleId, "${CONFUSING_IDENTIFIER_NAMING.warnText()} better name is: n1, n2", false),
            LintError(3, 9, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} Z", true),
            LintError(3, 9, ruleId, "${IDENTIFIER_LENGTH.warnText()} Z", false),
            LintError(7, 5, ruleId, "${CONFUSING_IDENTIFIER_NAMING.warnText()} better name is: bt, nxt", false),
            LintError(7, 5, ruleId, "${IDENTIFIER_LENGTH.warnText()} B", false))
    }

    @Test
    @Tag(WarningNames.GENERIC_NAME)
    fun `check generic types`() {
        val code =
            """
                interface Test<String>
                interface Test1<T: String>
                interface Test2<T : Collection<T>>
                interface Test3<out T>
                interface Test3<in T>
                interface Test4<in T, A, B>
                interface Test5<in T, A, Br>
                interface Test6<in Tr>
                interface Test6<Tr: String>
            """.trimIndent()
        lintMethod(code,
            LintError(1, 15, ruleId, "${GENERIC_NAME.warnText()} <String>", true),
            LintError(7, 16, ruleId, "${GENERIC_NAME.warnText()} <in T, A, Br>", true),
            LintError(8, 16, ruleId, "${GENERIC_NAME.warnText()} <in Tr>", true),
            LintError(9, 16, ruleId, "${GENERIC_NAME.warnText()} <Tr: String>", true),
        )
    }
}
