package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.constants.Warnings.BLANK_LINE_BETWEEN_PROPERTIES
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES
import org.cqfn.diktat.ruleset.rules.chapter3.ClassLikeStructuresOrderRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ClassLikeStructuresOrderRuleWarnTest : LintTestBase(::ClassLikeStructuresOrderRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${ClassLikeStructuresOrderRule.NAME_ID}"

    // ===== order of declarations =====

    private fun codeTemplate(keyword: String) = """
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

    @Test
    @Tag(WarningNames.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES)
    fun `should check order of declarations in classes - positive example`() {
        listOf("class", "interface", "object").forEach { keyword ->
            lintMethod(codeTemplate(keyword))
        }
    }

    @Test
    @Tag(WarningNames.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES)
    fun `should warn if loggers are not on top`() {
        lintMethod(
            """
                    |class Example {
                    |    private val FOO = 42
                    |    private val log = LoggerFactory.getLogger(Example.javaClass)
                    |}
            """.trimMargin(),
            LintError(2, 5, ruleId, "${WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES.warnText()} PROPERTY: FOO", true),
            LintError(3, 5, ruleId, "${WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES.warnText()} PROPERTY: log", true)
        )
    }

    // ===== comments on properties ======

    @Test
    @Tag(WarningNames.BLANK_LINE_BETWEEN_PROPERTIES)
    fun `comments and KDocs on properties should be prepended by newline - positive example`() {
        lintMethod(
            """
                    |class Example {
                    |    // logger property
                    |    private val log = LoggerFactory.getLogger(Example.javaClass)
                    |    private val FOO = 42
                    |
                    |    // another property
                    |    private val BAR = 43
                    |
                    |    @Annotated
                    |    private val qux = 43
                    |
                    |    // annotated property
                    |    @Annotated
                    |    private val quux = 43
                    |
                    |    /**
                    |     * Yet another property.
                    |     */
                    |    private val BAZ = 44
                    |    @Annotated private lateinit var lateFoo: Int
                    |}
            """.trimMargin())
    }

    @Test
    @Tag(WarningNames.BLANK_LINE_BETWEEN_PROPERTIES)
    fun `should warn if comments and KDocs on properties are not prepended by newline`() {
        lintMethod(
            """
                    |class Example {
                    |    private val log = LoggerFactory.getLogger(Example.javaClass)
                    |    private val FOO = 42
                    |    // another property
                    |    private val BAR = 43
                    |    @Anno
                    |    private val qux = 43
                    |    // annotated property
                    |    @Anno
                    |    private val quux = 43
                    |    /**
                    |     * Yet another property.
                    |     */
                    |    private val BAZ = 44
                    |    private lateinit var lateFoo: Int
                    |}
            """.trimMargin(),
            LintError(4, 5, ruleId, "${BLANK_LINE_BETWEEN_PROPERTIES.warnText()} BAR", true),
            LintError(6, 5, ruleId, "${BLANK_LINE_BETWEEN_PROPERTIES.warnText()} qux", true),
            LintError(8, 5, ruleId, "${BLANK_LINE_BETWEEN_PROPERTIES.warnText()} quux", true),
            LintError(11, 5, ruleId, "${BLANK_LINE_BETWEEN_PROPERTIES.warnText()} BAZ", true)
        )
    }

    @Test
    @Tag(WarningNames.BLANK_LINE_BETWEEN_PROPERTIES)
    fun `regression - should check only class-level and top-level properties`() {
        lintMethod(
            """class Example {
                    |    fun foo() {
                    |        val bar = 0
                    |
                    |        val baz = 1
                    |    }
                    |
                    |    class Nested {
                    |        val bar = 0
                    |        val baz = 1
                    |    }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.BLANK_LINE_BETWEEN_PROPERTIES)
    fun `should allow blank lines around properties with custom getters and setters - positive example`() {
        lintMethod(
            """
                    |class Example {
                    |    private val foo
                    |        get() = 0
                    |
                    |    private var backing = 0
                    |
                    |    var bar
                    |        get() = backing
                    |        set(value) { backing = value }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.BLANK_LINE_BETWEEN_PROPERTIES)
    fun `should allow blank lines around properties with custom getters and setters - positive example without blank lines`() {
        lintMethod(
            """
                    |class Example {
                    |    private val foo
                    |        get() = 0
                    |    private var backing = 0
                    |    var bar
                    |        get() = backing
                    |        set(value) { backing = value }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES)
    fun `should warn if order is incorrect and property with comment`() {
        lintMethod(
            """
                    class Example {
                        companion object {
                            val b = "q"

                            // this
                            private const val a = 3
                        }
                    }
            """.trimMargin(),
            LintError(3, 29, ruleId, "${WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES.warnText()} PROPERTY: b", true),
            LintError(5, 29, ruleId, "${WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES.warnText()} PROPERTY: a", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES)
    fun `should correctly check class elements order in enum classes`() {
        lintMethod(
            """
                enum class Enum {
                    FOO,
                    BAR,
                    ;

                    fun f() {}
                    companion object
                }
            """.trimMargin()
        )
    }
}
