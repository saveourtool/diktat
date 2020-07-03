package org.cqfn.diktat.ruleset.chapter1

import com.pinterest.ktlint.core.LintError
import org.junit.Test
import org.cqfn.diktat.ruleset.rules.IdentifierNaming
import org.cqfn.diktat.ruleset.constants.Warnings.*
import org.cqfn.diktat.ruleset.utils.lintMethod
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID

class IdentifierNamingWarnTest {

    private val ruleId: String = "$DIKTAT_RULE_SET_ID:identifier-naming"

    // ======== checks for generics ========
    @Test
    fun `generic class - single capital letter, can be followed by a number  (check - positive1)`() {
        val code =
            """
                    package org.cqfn.diktat.test

                    class TreeNode<T>(val value: T?, val next: TreeNode<T>? = null)

                """.trimIndent()
        lintMethod(IdentifierNaming(), code)
    }

    @Test
    fun `generic class - single capital letter, can be followed by a number  (check - positive2)`() {
        val code =
            """
                    package org.cqfn.diktat.test

                    class TreeNode<T123>(val value: T?, val next: TreeNode<T>? = null)

                """.trimIndent()
        lintMethod(IdentifierNaming(), code)
    }

    @Test
    fun `generic class - single capital letter, can be followed by a number  (check - negative1)`() {
        val code =
            """
                    package org.cqfn.diktat.test

                    class TreeNode<a>(val value: T?, val next: TreeNode<T>? = null)

                """.trimIndent()
        lintMethod(IdentifierNaming(), code, LintError(
            3, 15, ruleId, "${GENERIC_NAME.warnText()} <a>")
        )
    }

    @Test
    fun `generic class - single capital letter, can be followed by a number  (check - negative2)`() {
        val code =
            """
                    package org.cqfn.diktat.test

                    class TreeNode<TBBB>(val value: T?, val next: TreeNode<T>? = null)

                """.trimIndent()
        lintMethod(IdentifierNaming(), code, LintError(
            3, 15, ruleId, "${GENERIC_NAME.warnText()} <TBBB>")
        )
    }


    @Test
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
        lintMethod(IdentifierNaming(), code)
    }

    // ======== checks for variables and class names ========
    @Test
    fun `check class name (check)`() {
        val code =
            """
                    class incorrectNAME {}
                    class IncorrectNAME {}
                """
        lintMethod(IdentifierNaming(), code,
            LintError(2, 27, ruleId, "${CLASS_NAME_INCORRECT.warnText()} incorrectNAME"),
            LintError(3, 27, ruleId, "${CLASS_NAME_INCORRECT.warnText()} IncorrectNAME")
        )
    }

    @Test
    fun `check identifiers case format (check - negative)`() {
        val code =
            """
                  var SOMEtest = "TEST"
                  const val thisConstantShouldBeUpper = "CONST"
                  class className {
                      data class badClassName(val FIRST: String, var SECOND: String)

                      object companion {
                          const val incorrect_case = ""
                          val correctCase
                          var INCORRECT = ""
                      }

                      var check_me = ""
                      val CHECK_ME
                  }
                """.trimIndent()

        lintMethod(IdentifierNaming(), code,
            LintError(1, 5, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} SOMEtest"),
            LintError(2, 11, ruleId, "${CONSTANT_UPPERCASE.warnText()} thisConstantShouldBeUpper"),
            LintError(3, 7, ruleId, "${CLASS_NAME_INCORRECT.warnText()} className"),
            LintError(4, 16, ruleId, "${CLASS_NAME_INCORRECT.warnText()} badClassName"),
            LintError(4, 33, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} FIRST"),
            LintError(4, 52, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} SECOND"),
            LintError(7, 19, ruleId, "${CONSTANT_UPPERCASE.warnText()} incorrect_case"),
            LintError(9, 13, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} INCORRECT"),
            LintError(12, 9, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} check_me"),
            LintError(13, 9, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} CHECK_ME")
        )
    }

    @Test
    fun `check variable length (check - negative)`() {
        val code =
            """
                  val r = 0
                  val x256 = 256
                  val i = 1
                  class LongLongLongLongLongLongLongLongLongLongLongLongLongLongLongLongLongName {
                      val veryLongveryLongveryLongveryLongveryLongveryLongveryLongveryLongveryLongName = ""
                  }
                """.trimIndent()
        lintMethod(IdentifierNaming(), code,
            LintError(1, 5, ruleId, "${IDENTIFIER_LENGTH.warnText()} r"),
            LintError(2, 5, ruleId, "${VARIABLE_NAME_INCORRECT.warnText()} x256"),
            LintError(4, 7, ruleId, "${IDENTIFIER_LENGTH.warnText()} LongLongLongLongLongLongLongLongLongLongLongLongLongLongLongLongLongName"),
            LintError(5, 9, ruleId, "${IDENTIFIER_LENGTH.warnText()} veryLongveryLongveryLongveryLongveryLongveryLongveryLongveryLongveryLongName")
        )
    }


    @Test
    fun `check value parameters in dataclasses (check - negative)`() {
        val code =
            """
                    data class ClassName(val FIRST: String, var SECOND: String)
                """.trimIndent()
        lintMethod(IdentifierNaming(), code,
            LintError(1, 26, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} FIRST"),
            LintError(1, 45, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} SECOND")
        )
    }

    @Test
    fun `check value parameters in functions (check - negative)`() {
        val code =
            """
                    fun foo(SOMENAME: String) {
                    }
                """.trimIndent()
        lintMethod(IdentifierNaming(), code,
            LintError(1, 9, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} SOMENAME")
        )
    }

    @Test
    fun `check case for enum values (check - negative)`() {
        val code =
            """
                  enum class TEST_ONE {
                    first_value, secondValue, thirdVALUE
                  }
                """.trimIndent()
        lintMethod(IdentifierNaming(), code,
            LintError(1, 12, ruleId, "${CLASS_NAME_INCORRECT.warnText()} TEST_ONE"),
            LintError(2, 3, ruleId, "${ENUM_VALUE.warnText()} first_value"),
            LintError(2, 16, ruleId, "${ENUM_VALUE.warnText()} secondValue"),
            LintError(2, 29, ruleId, "${ENUM_VALUE.warnText()} thirdVALUE")
        )
    }

    @Test
    fun `check case for object (check - negative)`() {
        val code =
            """
                  object TEST_ONE {
                  }
                """.trimIndent()
        lintMethod(IdentifierNaming(), code,
            LintError(1, 8, ruleId, "${OBJECT_NAME_INCORRECT.warnText()} TEST_ONE")
        )
    }

    // ======== exception case and suffix ========
    @Test
    fun `check exception case format`() {
        val code =
            """
                    class incorrect_case_Exception(message: String) : Exception(message)
                """.trimIndent()
        lintMethod(IdentifierNaming(), code,
            LintError(1, 7, ruleId, "${CLASS_NAME_INCORRECT.warnText()} incorrect_case_Exception")
        )
    }

    @Test
    fun `check exception case and suffix (with type call entry) - negative`() {
        val code =
            """
                    class Custom(message: String) : Exception(message)
                """.trimIndent()
        lintMethod(IdentifierNaming(), code,
            LintError(1, 7, ruleId, "${EXCEPTION_SUFFIX.warnText()} Custom")
        )
    }

    @Test
    fun `check exception case and suffix (only parent name inheritance) - negative`() {
        val code =
            """
                    class Custom: Exception {
                        constructor(msg: String) : super(msg)
                    }
                """.trimIndent()
        lintMethod(IdentifierNaming(), code,
            LintError(1, 7, ruleId, "${EXCEPTION_SUFFIX.warnText()} Custom")
        )
    }

    @Test
    fun `checking that there should be no prefixes in variable name`() {
        val code =
            """
                    const val M_GLOB = ""
                    val aPrefix = ""
                """.trimIndent()

        lintMethod(IdentifierNaming(), code,
            LintError(1, 11, ruleId, "${VARIABLE_HAS_PREFIX.warnText()} M_GLOB"),
            LintError(2, 5, ruleId, "${VARIABLE_HAS_PREFIX.warnText()} aPrefix")
        )
    }
}
