package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.Warnings.BLANK_LINE_BETWEEN_PROPERTIES
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES
import org.cqfn.diktat.ruleset.rules.ClassLikeStructuresOrderRule
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ClassLikeStructuresOrderRuleWarnTest {
    private val ruleId = "$DIKTAT_RULE_SET_ID:class-like-structures"

    // ===== order of declarations =====

    @Test
    @Tag("WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES")
    fun `should check order of declarations in classes - positive example`() {
        fun codeTemplate(keyword: String) = """
                    |$keyword Example {
                    |    private val log = LoggerFactory.getLogger(Example.javaClass)
                    |    private val FOO = 42
                    |    private lateinit var lateFoo: Int
                    |    init {
                    |        bar()
                    |    }
                    |    constructor(baz: Int)
                    |    fun foo() {
                    |        val nested = Nested()
                    |    }
                    |    class Nested {
                    |        val nestedFoo = 43
                    |    }
                    |    companion object {
                    |        private const val ZERO = 0
                    |        private var i = 0
                    |    }
                    |    class UnusedNested { }
                    |}
                """.trimMargin()
        listOf("class", "interface", "object").forEach { keyword ->
            lintMethod(ClassLikeStructuresOrderRule(), codeTemplate(keyword))
        }
    }

    @Test
    @Tag("WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES")
    fun `should warn if loggers are not on top`() {
        listOf("private ", "").forEach { modifier ->
            lintMethod(ClassLikeStructuresOrderRule(),
                    """
                    |class Example {
                    |    private val FOO = 42
                    |    ${modifier}val log = LoggerFactory.getLogger(Example.javaClass)
                    |}
                """.trimMargin(),
                    LintError(2, 5, ruleId, "${WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES.warnText()} PROPERTY: private val FOO = 42", true),
                    LintError(3, 5, ruleId, "${WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES.warnText()} PROPERTY: ${modifier}val log = LoggerFactory.getLogger(Example.javaClass)", true)
            )
        }
    }

    // ===== comments on properties ======

    @Test
    @Tag("BLANK_LINE_BETWEEN_PROPERTIES")
    fun `comments and KDocs on properties should be prepended by newline - positive example`() {
        lintMethod(ClassLikeStructuresOrderRule(),
                """
                    |class Example {
                    |    // logger property
                    |    private val log = LoggerFactory.getLogger(Example.javaClass)
                    |    private val FOO = 42
                    |    
                    |    // another property
                    |    private val BAR = 43
                    |    
                    |    /**
                    |     * Yet another property.
                    |     */
                    |    private val BAZ = 44
                    |    private lateinit var lateFoo: Int
                    |}
                """.trimMargin())
    }

    @Test
    @Tag("BLANK_LINE_BETWEEN_PROPERTIES")
    fun `should warn if comments and KDocs on properties are not prepended by newline`() {
        lintMethod(ClassLikeStructuresOrderRule(),
                """
                    |class Example {
                    |    private val log = LoggerFactory.getLogger(Example.javaClass)
                    |    private val FOO = 42
                    |    // another property
                    |    private val BAR = 43
                    |    /**
                    |     * Yet another property.
                    |     */
                    |    private val BAZ = 44
                    |    private lateinit var lateFoo: Int
                    |}
                """.trimMargin(),
                LintError(4, 5, ruleId, "${BLANK_LINE_BETWEEN_PROPERTIES.warnText()} BAR", true),
                LintError(6, 5, ruleId, "${BLANK_LINE_BETWEEN_PROPERTIES.warnText()} BAZ", true)
        )
    }
}
