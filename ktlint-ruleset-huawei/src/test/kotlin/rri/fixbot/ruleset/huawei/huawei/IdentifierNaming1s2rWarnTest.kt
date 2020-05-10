package rri.fixbot.ruleset.huawei.huawei

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import rri.fixbot.ruleset.huawei.IdentifierNaming1s2r
import rri.fixbot.ruleset.huawei.constants.Warnings.*

class IdentifierNaming1s2rWarnTest {
    // ======== checks for generics ========
    @Test
    fun `generic class - single capital letter, can be followed by a number  (check - positive1)`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                    package com.huawei.test

                    class TreeNode<T>(val value: T?, val next: TreeNode<T>? = null)

                """.trimIndent()
            )
        ).isEmpty()
    }

    @Test
    fun `generic class - single capital letter, can be followed by a number  (check - positive2)`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                    package com.huawei.test

                    class TreeNode<T123>(val value: T?, val next: TreeNode<T>? = null)

                """.trimIndent()
            )
        ).isEmpty()
    }

    @Test
    fun `generic class - single capital letter, can be followed by a number  (check - negative1)`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                    package com.huawei.test

                    class TreeNode<a>(val value: T?, val next: TreeNode<T>? = null)

                """.trimIndent()
            )
        ).containsExactly(LintError(
            3, 15, "identifier-naming", "${GENERIC_NAME.text} <a>")
        )
    }

    @Test
    fun `generic class - single capital letter, can be followed by a number  (check - negative2)`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                    package com.huawei.test

                    class TreeNode<TBBB>(val value: T?, val next: TreeNode<T>? = null)

                """.trimIndent()
            )
        ).containsExactly(LintError(
            3, 15, "identifier-naming", "${GENERIC_NAME.text} <TBBB>")
        )
    }


    @Test
    fun `generic method - single capital letter, can be followed by a number  (check)`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                   package com.huawei.test

                   fun <T> makeLinkedList(vararg elements: T): TreeNode<T>? {
                        var node: TreeNode<T>? = null
                        for (element in elements.reversed()) {
                             node = TreeNode(element, node)
                        }
                        return node
                    }
                """.trimIndent()
            )
        ).isEmpty()
    }

    // ======== checks for variables and class names ========
    @Test
    fun `check identifiers case format (check - negative)`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                  var SOMEtest = "TEST"
                  val thisConstantShouldBeUpper = "CONST"
                  class className {
                      data class badClassName(val FIRST: String, var SECOND: String)

                      object companion {
                          val incorrect_case = ""
                          var INCORRECT = ""
                      }

                      var check_me = ""
                      val CHECK_ME
                  }
                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 5, "identifier-naming", "${VARIABLE_NAME_INCORRECT_FORMAT.text} SOMEtest"),
            LintError(2, 5, "identifier-naming", "${CONSTANT_UPPERCASE.text} thisConstantShouldBeUpper"),
            LintError(3, 7, "identifier-naming", "${CLASS_NAME_INCORRECT.text} className"),
            LintError(4, 16, "identifier-naming", "${CLASS_NAME_INCORRECT.text} badClassName"),
            LintError(4, 33, "identifier-naming", "${VARIABLE_NAME_INCORRECT_FORMAT.text} FIRST"),
            LintError(4, 52, "identifier-naming", "${VARIABLE_NAME_INCORRECT_FORMAT.text} SECOND"),
            LintError(7, 13, "identifier-naming", "${CONSTANT_UPPERCASE.text} incorrect_case"),
            LintError(8, 13, "identifier-naming", "${VARIABLE_NAME_INCORRECT_FORMAT.text} INCORRECT"),
            LintError(11, 9, "identifier-naming", "${VARIABLE_NAME_INCORRECT_FORMAT.text} check_me"),
            LintError(12, 9, "identifier-naming", "${VARIABLE_NAME_INCORRECT_FORMAT.text} CHECK_ME")
        )
    }

    @Test
    fun `check variable length (check - negative)`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                  val r = 0
                  val x256 = 256
                  val i = 1
                  class LongLongLongLongLongLongLongLongLongLongLongLongLongLongLongLongLongName {
                      val veryLongveryLongveryLongveryLongveryLongveryLongveryLongveryLongveryLongName = ""
                  }
                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 5, "identifier-naming", "${IDENTIFIER_LENGTH.text} r"),
            LintError(2, 5, "identifier-naming", "${VARIABLE_NAME_INCORRECT.text} x256"),
            LintError(2, 5, "identifier-naming", "${CONSTANT_UPPERCASE.text} x256"),
            LintError(4, 7, "identifier-naming", "${IDENTIFIER_LENGTH.text} LongLongLongLongLongLongLongLongLongLongLongLongLongLongLongLongLongName"),
            LintError(5, 9, "identifier-naming", "${IDENTIFIER_LENGTH.text} veryLongveryLongveryLongveryLongveryLongveryLongveryLongveryLongveryLongName")
        )
    }


    @Test
    fun `check value parameters in dataclasses (check - negative)`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                    data class ClassName(val FIRST: String, var SECOND: String)
                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 26, "identifier-naming", "${VARIABLE_NAME_INCORRECT_FORMAT.text} FIRST"),
            LintError(1, 45, "identifier-naming", "${VARIABLE_NAME_INCORRECT_FORMAT.text} SECOND")
        )
    }

    @Test
    fun `check value parameters in functions (check - negative)`() {
        assertThat(
            IdentifierNaming1s2r().lint(
                """
                    fun foo(SOMENAME: String) {
                    }
                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 9, "identifier-naming", "${FUNCTION_BOOLEAN_PREFIX.text} SOMENAME")
        )
    }
}
