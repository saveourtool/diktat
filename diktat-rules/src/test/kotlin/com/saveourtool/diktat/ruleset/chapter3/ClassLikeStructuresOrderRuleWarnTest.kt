package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings.BLANK_LINE_BETWEEN_PROPERTIES
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES
import com.saveourtool.diktat.ruleset.rules.chapter3.ClassLikeStructuresOrderRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import com.saveourtool.diktat.test.framework.util.describe
import generated.WarningNames
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ClassLikeStructuresOrderRuleWarnTest : LintTestBase(::ClassLikeStructuresOrderRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${ClassLikeStructuresOrderRule.NAME_ID}"

    // ===== order of declarations =====

    @Language("kotlin")
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
            DiktatError(2, 5, ruleId, "${WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES.warnText()} PROPERTY: FOO", true),
            DiktatError(3, 5, ruleId, "${WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES.warnText()} PROPERTY: log", true)
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
            DiktatError(4, 5, ruleId, "${BLANK_LINE_BETWEEN_PROPERTIES.warnText()} BAR", true),
            DiktatError(6, 5, ruleId, "${BLANK_LINE_BETWEEN_PROPERTIES.warnText()} qux", true),
            DiktatError(8, 5, ruleId, "${BLANK_LINE_BETWEEN_PROPERTIES.warnText()} quux", true),
            DiktatError(11, 5, ruleId, "${BLANK_LINE_BETWEEN_PROPERTIES.warnText()} BAZ", true)
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
    fun `a single-line comment after annotation`() {
        lintMethod(
            """class Example {
                    |   private val val0 = Regex(""${'"'}\d+""${'"'})
                    |
                    |    @Deprecated("Deprecation message") // Trailing comment
                    |    private val val2 = ""
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
    @Tag(WarningNames.BLANK_LINE_BETWEEN_PROPERTIES)
    fun `should not trigger on EOL comment on the same line with property`() {
        lintMethod(
            """
                    |class ActiveBinsMetric(meterRegistry: MeterRegistry, private val binRepository: BinRepository) {
                    |    companion object {
                    |        private const val EGG_7_9_BUCKET_LABEL = "7-9"
                    |        private const val DELAY = 15000L  // 15s
                    |    }
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
            DiktatError(3, 29, ruleId, "${WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES.warnText()} PROPERTY: b", true),
            DiktatError(5, 29, ruleId, "${WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES.warnText()} PROPERTY: a", true)
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

    /**
     * An exception to the "loggers on top" rule.
     *
     * See [#1516](https://github.com/saveourtool/diktat/issues/1516).
     */
    @Test
    @Tag(WarningNames.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES)
    fun `logger-like const property should not be reordered`() {
        val code = """
            |object C {
            |    private const val A = "value"
            |
            |    // Not a logger
            |    private const val LOG = "value"
            |}
        """.trimMargin()

        val actualErrors = lintResult(code)
        assertThat(actualErrors)
            .describedAs("lint result for ${code.describe()}")
            .isEmpty()
    }

    /**
     * An exception to the "loggers on top" rule.
     *
     * See [#1516](https://github.com/saveourtool/diktat/issues/1516).
     */
    @Test
    @Tag(WarningNames.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES)
    fun `logger-like lateinit property should not be reordered`() {
        val code = """
            |object C {
            |    private lateinit val A
            |    private lateinit val LOG // Not a logger
            |}
        """.trimMargin()

        val actualErrors = lintResult(code)
        assertThat(actualErrors)
            .describedAs("lint result for ${code.describe()}")
            .isEmpty()
    }

    /**
     * An exception to the "loggers on top" rule.
     *
     * See [#1516](https://github.com/saveourtool/diktat/issues/1516).
     */
    @Test
    @Tag(WarningNames.WRONG_ORDER_IN_CLASS_LIKE_STRUCTURES)
    fun `property with a name containing 'log' is not a logger`() {
        val code = """
            |object C {
            |    private val a = System.getProperty("os.name")
            |    private val loginName = LoggerFactory.getLogger({}.javaClass)
            |}
        """.trimMargin()

        val actualErrors = lintResult(code)
        assertThat(actualErrors)
            .describedAs("lint result for ${code.describe()}")
            .isEmpty()
    }
}
