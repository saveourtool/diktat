package rri.fixbot.ruleset.huawei.huawei

import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.test.lint
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import rri.fixbot.ruleset.huawei.IdentifierNaming1s2r
import rri.fixbot.ruleset.huawei.constants.Warnings.*

class IdentifierNaming1s2rWarnTest {
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

}
