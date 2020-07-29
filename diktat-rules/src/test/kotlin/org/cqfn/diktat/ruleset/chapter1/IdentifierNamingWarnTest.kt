package org.cqfn.diktat.ruleset.chapter1

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.ast.ElementType
import org.junit.Test
import org.cqfn.diktat.ruleset.rules.IdentifierNaming
import org.cqfn.diktat.ruleset.constants.Warnings.*
import org.cqfn.diktat.util.lintMethod
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.comments.CommentsRule
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.TokenType
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.ImportPath

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

                      companion object {
                          const val incorrect_case = ""
                          val correctCase = ""
                          var INCORRECT = ""
                      }

                      var check_me = ""
                      val CHECK_ME = ""
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

    @Test
    fun `regression - checking that digit in the end will not raise a warning`() {
        val code =
                """
                    val I_AM_CONSTANT1  = ""
                    const val I_AM_CONSTANT2  = ""
                """.trimIndent()

        lintMethod(IdentifierNaming(), code,
                LintError(1, 5, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} I_AM_CONSTANT1"),
                LintError(1, 5, ruleId, "${VARIABLE_HAS_PREFIX.warnText()} I_AM_CONSTANT1"),
                LintError(2, 11, ruleId, "${VARIABLE_HAS_PREFIX.warnText()} I_AM_CONSTANT2")
        )
    }

    @Test
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

        lintMethod(IdentifierNaming(), code,
                LintError(5, 13, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} STRANGECASE")
        )
    }

    @Test
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

        lintMethod(IdentifierNaming(), code,
                LintError(3, 35, ruleId, "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} STRANGECASE")
        )
    }

    @Test
    fun `FUNCTION_BOOLEAN_PREFIX - positive example`() {
        lintMethod(IdentifierNaming(),
                """
                    fun ASTNode.hasEmptyLineAfter(): Boolean { }    
                    fun hasEmptyLineAfter(): Boolean { }    
                    fun ASTNode.isEmpty(): Boolean { }    
                    fun isEmpty(): Boolean { }    
                """.trimIndent()
        )
    }

    @Test
    fun `FUNCTION_BOOLEAN_PREFIX - negative example`() {
        lintMethod(IdentifierNaming(),
                """
                    fun ASTNode.emptyLineAfter(): Boolean { }    
                    fun emptyLineAfter(): Boolean { }    
                    fun ASTNode.empty(): Boolean { }    
                    fun empty(): Boolean { }    
                """.trimIndent(),
                LintError(1, 13, ruleId, "${FUNCTION_BOOLEAN_PREFIX.warnText()} emptyLineAfter"),
                LintError(2, 5, ruleId, "${FUNCTION_BOOLEAN_PREFIX.warnText()} emptyLineAfter"),
                LintError(3, 13, ruleId, "${FUNCTION_BOOLEAN_PREFIX.warnText()} empty"),
                LintError(4, 5, ruleId, "${FUNCTION_BOOLEAN_PREFIX.warnText()} empty")
        )
    }
    @Test
    fun `regression - function argument type`() {
        // valid example, should not cause exceptions
        lintMethod(IdentifierNaming(),
                """
                    fun foo(predicate: (Int) -> Boolean) = Unit    
                """.trimIndent()
        )

        // identifier names in function types are still checked if present
        lintMethod(IdentifierNaming(),
                """
                    fun foo(predicate: (a: Int) -> Boolean) = Unit    
                """.trimIndent(),
                LintError(1, 21, ruleId, "${IDENTIFIER_LENGTH.warnText()} a", false)
        )
    }
}
