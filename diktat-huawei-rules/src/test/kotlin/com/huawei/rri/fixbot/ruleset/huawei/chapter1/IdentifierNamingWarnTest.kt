package com.huawei.rri.fixbot.ruleset.huawei.chapter1

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.huawei.rri.fixbot.ruleset.huawei.rules.IdentifierNaming
import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.*

class IdentifierNamingWarnTest {
    // ======== checks for generics ========
    @Test
    fun `generic class - single capital letter, can be followed by a number  (check - positive1)`() {
        assertThat(
            IdentifierNaming().lint(
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
            IdentifierNaming().lint(
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
            IdentifierNaming().lint(
                """
                    package com.huawei.test

                    class TreeNode<a>(val value: T?, val next: TreeNode<T>? = null)

                """.trimIndent()
            )
        ).containsExactly(LintError(
            3, 15, "identifier-naming", "${GENERIC_NAME.warnText()} <a>")
        )
    }

    @Test
    fun `generic class - single capital letter, can be followed by a number  (check - negative2)`() {
        assertThat(
            IdentifierNaming().lint(
                """
                    package com.huawei.test

                    class TreeNode<TBBB>(val value: T?, val next: TreeNode<T>? = null)

                """.trimIndent()
            )
        ).containsExactly(LintError(
            3, 15, "identifier-naming", "${GENERIC_NAME.warnText()} <TBBB>")
        )
    }


    @Test
    fun `generic method - single capital letter, can be followed by a number  (check)`() {
        assertThat(
            IdentifierNaming().lint(
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
    fun `check class name (check)`() {
        assertThat(
            IdentifierNaming().lint(
                """
                    class incorrectNAME {}
                    class IncorrectNAME {}
                """
            )
        ).containsExactly(
            LintError(2, 27, "identifier-naming", "${CLASS_NAME_INCORRECT.warnText()} incorrectNAME"),
            LintError(3, 27, "identifier-naming", "${CLASS_NAME_INCORRECT.warnText()} IncorrectNAME")
        )
    }

    @Test
    fun `check identifiers case format (check - negative)`() {
        assertThat(
            IdentifierNaming().lint(
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
            )
        ).containsExactly(
            LintError(1, 5, "identifier-naming", "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} SOMEtest"),
            LintError(2, 11, "identifier-naming", "${CONSTANT_UPPERCASE.warnText()} thisConstantShouldBeUpper"),
            LintError(3, 7, "identifier-naming", "${CLASS_NAME_INCORRECT.warnText()} className"),
            LintError(4, 16, "identifier-naming", "${CLASS_NAME_INCORRECT.warnText()} badClassName"),
            LintError(4, 33, "identifier-naming", "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} FIRST"),
            LintError(4, 52, "identifier-naming", "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} SECOND"),
            LintError(7, 19, "identifier-naming", "${CONSTANT_UPPERCASE.warnText()} incorrect_case"),
            LintError(9, 13, "identifier-naming", "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} INCORRECT"),
            LintError(12, 9, "identifier-naming", "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} check_me"),
            LintError(13, 9, "identifier-naming", "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} CHECK_ME")
        )
    }

    @Test
    fun `check variable length (check - negative)`() {
        assertThat(
            IdentifierNaming().lint(
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
            LintError(1, 5, "identifier-naming", "${IDENTIFIER_LENGTH.warnText()} r"),
            LintError(2, 5, "identifier-naming", "${VARIABLE_NAME_INCORRECT.warnText()} x256"),
            LintError(4, 7, "identifier-naming", "${IDENTIFIER_LENGTH.warnText()} LongLongLongLongLongLongLongLongLongLongLongLongLongLongLongLongLongName"),
            LintError(5, 9, "identifier-naming", "${IDENTIFIER_LENGTH.warnText()} veryLongveryLongveryLongveryLongveryLongveryLongveryLongveryLongveryLongName")
        )
    }


    @Test
    fun `check value parameters in dataclasses (check - negative)`() {
        assertThat(
            IdentifierNaming().lint(
                """
                    data class ClassName(val FIRST: String, var SECOND: String)
                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 26, "identifier-naming", "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} FIRST"),
            LintError(1, 45, "identifier-naming", "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} SECOND")
        )
    }

    @Test
    fun `check value parameters in functions (check - negative)`() {
        assertThat(
            IdentifierNaming().lint(
                """
                    fun foo(SOMENAME: String) {
                    }
                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 9, "identifier-naming", "${VARIABLE_NAME_INCORRECT_FORMAT.warnText()} SOMENAME")
        )
    }

    @Test
    fun `check case for enum values (check - negative)`() {
        assertThat(
            IdentifierNaming().lint(
                """
                  enum class TEST_ONE {
                    first_value, secondValue, thirdVALUE
                  }
                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 12, "identifier-naming", "${CLASS_NAME_INCORRECT.warnText()} TEST_ONE"),
            LintError(2, 3, "identifier-naming", "${ENUM_VALUE.warnText()} first_value"),
            LintError(2, 16, "identifier-naming", "${ENUM_VALUE.warnText()} secondValue"),
            LintError(2, 29, "identifier-naming", "${ENUM_VALUE.warnText()} thirdVALUE")
        )
    }

    @Test
    fun `check case for object (check - negative)`() {
        assertThat(
            IdentifierNaming().lint(
                """
                  object TEST_ONE {
                  }
                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 8, "identifier-naming", "${OBJECT_NAME_INCORRECT.warnText()} TEST_ONE")
        )
    }

    // ======== exception case and suffix ========
    @Test
    fun `check exception case format`() {
        assertThat(
            IdentifierNaming().lint(
                """
                    class incorrect_case_Exception(message: String) : Exception(message)
                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 7, "identifier-naming", "${CLASS_NAME_INCORRECT.warnText()} incorrect_case_Exception")
        )
    }

    @Test
    fun `check exception case and suffix (with type call entry) - negative`() {
        assertThat(
            IdentifierNaming().lint(
                """
                    class Custom(message: String) : Exception(message)
                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 7, "identifier-naming", "${EXCEPTION_SUFFIX.warnText()} Custom")
        )
    }

    @Test
    fun `check exception case and suffix (only parent name inheritance) - negative`() {
        assertThat(
            IdentifierNaming().lint(
                """
                    class Custom: Exception {
                        constructor(msg: String) : super(msg)
                    }
                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 7, "identifier-naming", "${EXCEPTION_SUFFIX.warnText()} Custom")
        )
    }

    @Test
    fun `checking that there should be no prefixes in variable name`() {
        assertThat(
            IdentifierNaming().lint(
                """
                    const val M_GLOB = ""
                    val aPrefix = ""
                """.trimIndent()
            )
        ).containsExactly(
            LintError(1, 11, "identifier-naming", "${VARIABLE_HAS_PREFIX.warnText()} M_GLOB"),
            LintError(2, 5, "identifier-naming", "${VARIABLE_HAS_PREFIX.warnText()} aPrefix")
        )
    }
}
