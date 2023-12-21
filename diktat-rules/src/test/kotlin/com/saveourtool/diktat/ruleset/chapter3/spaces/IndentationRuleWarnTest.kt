package com.saveourtool.diktat.ruleset.chapter3.spaces

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import com.saveourtool.diktat.ruleset.junit.NaturalDisplayName
import com.saveourtool.diktat.ruleset.rules.chapter3.files.IndentationRule
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.ALIGNED_PARAMETERS
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_AFTER_OPERATORS
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_BEFORE_DOT
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_FOR_EXPRESSION_BODIES
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.EXTENDED_INDENT_OF_PARAMETERS
import com.saveourtool.diktat.ruleset.utils.indentation.IndentationConfig.Companion.INDENTATION_SIZE
import com.saveourtool.diktat.util.LintTestBase
import com.saveourtool.diktat.util.TEST_FILE_NAME

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

/**
 * Legacy indentation tests.
 *
 * Consider adding new tests to [IndentationRuleTest] instead.
 *
 * @see IndentationRuleTest
 */
@Suppress("LargeClass")
@TestMethodOrder(NaturalDisplayName::class)
class IndentationRuleWarnTest : LintTestBase(::IndentationRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${IndentationRule.NAME_ID}"
    private val rulesConfigList = listOf(
        RulesConfig(WRONG_INDENTATION.name, true,
            mapOf(
                EXTENDED_INDENT_OF_PARAMETERS to "true",
                ALIGNED_PARAMETERS to "true",
                EXTENDED_INDENT_FOR_EXPRESSION_BODIES to "true",
                EXTENDED_INDENT_AFTER_OPERATORS to "true",
                EXTENDED_INDENT_BEFORE_DOT to "false",
                INDENTATION_SIZE to "4"
            )
        )
    )
    private val disabledOptionsRulesConfigList = listOf(
        RulesConfig(WRONG_INDENTATION.name, true,
            mapOf(
                EXTENDED_INDENT_OF_PARAMETERS to "false",
                ALIGNED_PARAMETERS to "false",
                EXTENDED_INDENT_FOR_EXPRESSION_BODIES to "false",
                EXTENDED_INDENT_AFTER_OPERATORS to "false",
                EXTENDED_INDENT_BEFORE_DOT to "false",
                INDENTATION_SIZE to "4"
            )
        )
    )

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `should warn if tabs are used in indentation`() {
        lintMethod(
            """
                    |class Example {
                    |${"\t"}val zero = 0
                    |}
                    |
            """.trimMargin(),
            DiktatError(2, 1, ruleId, "${WRONG_INDENTATION.warnText()} tabs are not allowed for indentation", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `should warn if indent size is not 4 spaces`() {
        lintMethod(
            """
                    |class Example {
                    |   val zero = 0
                    |}
                    |
            """.trimMargin(),
            DiktatError(2, 1, ruleId, warnText(4, 3), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `should warn if no new line at the end of file`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                    |class Example {
                    |    val zero = 0
                    |}
            """.trimMargin(),
            tempDir = tempDir,
            fileName = TEST_FILE_NAME,
            DiktatError(3, 1, ruleId, "${WRONG_INDENTATION.warnText()} no newline at the end of file $TEST_FILE_NAME", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `should warn if no new line at the end of file, last child whitespace`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                |class Example {
                |    val zero = 0
                |}
            """.trimMargin(),
            tempDir = tempDir,
            fileName = TEST_FILE_NAME,
            DiktatError(3, 1, ruleId, "${WRONG_INDENTATION.warnText()} no newline at the end of file $TEST_FILE_NAME", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `should warn if too many blank lines at the end of file`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                    |class Example {
                    |    val zero = 0
                    |}
                    |
                    |
            """.trimMargin(),
            tempDir = tempDir,
            fileName = TEST_FILE_NAME,
            DiktatError(5, 1, ruleId, "${WRONG_INDENTATION.warnText()} too many blank lines at the end of file $TEST_FILE_NAME", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `valid indentation - example 1`() {
        lintMethod(
            """
                    |class Example {
                    |    private val foo = 0
                    |    private val fuu =
                    |        0
                    |
                    |    fun bar() {
                    |        if (foo > 0) {
                    |            baz()
                    |        } else {
                    |            bazz()
                    |        }
                    |        return foo
                    |    }
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `parameters can be indented by 8 spaces - positive example`() {
        lintMethod(
            """
                    |class Example(
                    |        val field1: Type1,
                    |        val field2: Type2,
                    |        val field3: Type3
                    |) {
                    |    val e1 = Example(
                    |            t1,
                    |            t2,
                    |            t3
                    |    )
                    |
                    |    val e2 = Example(t1, t2,
                    |            t3
                    |    )
                    |}
                    |
            """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `parameters can be aligned - positive example`() {
        lintMethod(
            """
                    |class Example(val field1: Type1,
                    |              val field2: Type2,
                    |              val field3: Type3) {
                    |}
                    |
            """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `parameters can be aligned`() {
        lintMethod(
            """
                    |class Example(
                    |              val field1: Type1,
                    |              val field2: Type2,
                    |              val field3: Type3) {
                    |}
                    |
            """.trimMargin(),
            DiktatError(2, 1, ruleId, warnText(8, 14), true),
            DiktatError(3, 1, ruleId, warnText(8, 14), true),
            DiktatError(4, 1, ruleId, warnText(8, 14), true),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `lines split by operator can be indented by 8 spaces`() {
        lintMethod(
            """
                    |fun foo(a: Int, b: Int) {
                    |    return 2 * a +
                    |            b
                    |}
                    |
            """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `should check indentation in KDocs - positive example`() {
        lintMethod(
            """
                    |/**
                    | * Lorem ipsum
                    | */
                    |class Example {
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `assignment increases indentation if followed by newline`() {
        lintMethod(
            """
                    |fun <T> foo(list: List<T>) {
                    |    val a = list.filter {
                    |        predicate(it)
                    |    }
                    |
                    |    val b =
                    |        list.filter {
                    |            predicate(it)
                    |        }
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `when lambda is assigned, indentation is increased by one step`() {
        lintMethod(
            """
                    |fun foo() {
                    |    val a = { x: Int ->
                    |        x * 2
                    |    }
                    |
                    |    val b =
                    |        { x: Int ->
                    |            x * 2
                    |        }
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `should check indentation in KDocs`() {
        lintMethod(
            """
                    |/**
                    |* Lorem ipsum
                    |*/
                    |class Example {
                    |}
                    |
            """.trimMargin(),
            DiktatError(2, 1, ruleId, warnText(1, 0), true),
            DiktatError(3, 1, ruleId, warnText(1, 0), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `dot call increases indentation`() {
        lintMethod(
            """
                    |fun foo() {
                    |    Integer
                    |        .valueOf(2).also {
                    |            println(it)
                    |        }
                    |        ?.also {
                    |            println("Also with safe access")
                    |        }
                    |        ?: Integer.valueOf(0)
                    |
                    |    bar
                    |        .baz()
                    |            as Baz
                    |            as? Baz
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `loops and conditionals without braces should be indented - positive example`() {
        lintMethod(
            """
                    |fun foo() {
                    |    for (i in 1..100)
                    |        println(i)
                    |
                    |    do
                    |        println()
                    |    while (condition)
                    |
                    |    if (condition)
                    |        bar()
                    |    else
                    |        baz()
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `loops and conditionals without braces should be indented`() {
        lintMethod(
            """
                    |fun foo() {
                    |    for (i in 1..100)
                    |    println(i)
                    |
                    |    do
                    |    println()
                    |    while (condition)
                    |
                    |    if (condition)
                    |    bar()
                    |    else
                    |    baz()
                    |}
                    |
            """.trimMargin(),
            DiktatError(3, 1, ruleId, warnText(8, 4), true),
            DiktatError(6, 1, ruleId, warnText(8, 4), true),
            DiktatError(10, 1, ruleId, warnText(8, 4), true),
            DiktatError(12, 1, ruleId, warnText(8, 4), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `loops and conditionals without braces should be indented - if-else with mixed braces`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (condition) {
                    |        bar()
                    |    } else
                    |        baz()
                    |
                    |    if (condition)
                    |        bar()
                    |    else {
                    |        baz()
                    |    }
                    |
                    |    if (condition)
                    |        bar()
                    |    else if (condition2) {
                    |        baz()
                    |    } else
                    |        qux()
                    |
                    |    if (condition)
                    |        bar()
                    |    else if (condition2)
                    |        baz()
                    |    else {
                    |        quux()
                    |    }
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `opening braces should not increase indent when placed on the same line`() {
        lintMethod(
            """
                    |fun foo() {
                    |    consume(Example(
                    |        t1, t2, t3
                    |    ))
                    |
                    |    bar(baz(
                    |        1,
                    |        2
                    |    )
                    |    )
                    |
                    |    bar(baz(
                    |        1,
                    |        2),
                    |        3
                    |    )
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `opening braces should not increase indent when placed on the same line - with disabled options`() {
        lintMethod(
            """
                    |fun foo() {
                    |    bar(baz(
                    |        1,
                    |        2),
                    |        3
                    |    )
                    |}
                    |
            """.trimMargin(),
            rulesConfigList = disabledOptionsRulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `custom getters and setters should increase indentation - positive example`() {
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
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `custom getters and setters should increase indentation`() {
        lintMethod(
            """
                    |class Example {
                    |    private val foo
                    |            get() = 0
                    |
                    |    private var backing = 0
                    |
                    |    var bar
                    |    get() = backing
                    |    set(value) { backing = value }
                    |}
                    |
            """.trimMargin(),
            DiktatError(3, 1, ruleId, warnText(8, 12), true),
            DiktatError(8, 1, ruleId, warnText(8, 4), true),
            DiktatError(9, 1, ruleId, warnText(8, 4), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `regression - indentation should be increased inside parameter list for multiline parameters`() {
        lintMethod(
            """
                    |fun foo() {
                    |    bar(
                    |        param1 = baz(
                    |            1,
                    |            2
                    |        ),
                    |        param2 = { elem ->
                    |            elem.qux()
                    |        },
                    |        param3 = x
                    |            .y()
                    |    )
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `regression - nested blocks inside loops and conditionals without braces should be properly indented`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (condition)
                    |        list.filter {
                    |            bar()
                    |        }
                    |            .call(
                    |                param1,
                    |                param2
                    |            )
                    |    else
                    |        list
                    |            .filter {
                    |                baz()
                    |            }
                    |}
                    |
            """.trimMargin(),
            rulesConfigList = listOf(
                RulesConfig(WRONG_INDENTATION.name, true,
                    mapOf(
                        EXTENDED_INDENT_OF_PARAMETERS to "false",
                        EXTENDED_INDENT_BEFORE_DOT to "false"
                    )
                )
            )
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `arrows in when expression should increase indentation - positive example`() {
        lintMethod(
            """
                    |fun foo() {
                    |    when (x) {
                    |        X_1 ->
                    |            foo(x)
                    |        X_2 -> bar(x)
                    |        X_3 -> {
                    |            baz(x)
                    |        }
                    |        else ->
                    |            qux(x)
                    |    }
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `arrows in when expression should increase indentation`() {
        lintMethod(
            """
                    |fun foo() {
                    |    when (x) {
                    |        X_1 ->
                    |        foo(x)
                    |        X_2 -> bar(x)
                    |        X_3 -> {
                    |        baz(x)
                    |        }
                    |        else ->
                    |        qux(x)
                    |    }
                    |}
                    |
            """.trimMargin(),
            DiktatError(4, 1, ruleId, warnText(12, 8), true),
            DiktatError(7, 1, ruleId, warnText(12, 8), true),
            DiktatError(10, 1, ruleId, warnText(12, 8), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `comments should not turn off exceptional indentation`() {
        lintMethod(
            """
                    |fun foo() {
                    |    list
                    |        .map(::foo)
                    |        // comment about the next call
                    |        .filter { it.bar() }
                    |        // another comment about the next call
                    |        ?.filter { it.bar() }
                    |        ?.count()
                    |
                    |    list.any { predicate(it) } &&
                    |            list.any {
                    |                predicate(it)
                    |            }
                    |
                    |    list.any { predicate(it) } &&
                    |            // comment
                    |            list.any {
                    |                predicate(it)
                    |            }
                    |
                    |    list.filter {
                    |        predicate(it) &&
                    |                // comment
                    |                predicate(it)
                    |    }
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `regression - npe with comments`() {
        lintMethod(
            """
                |fun foo() {
                |    bar.let {
                |        baz(it)
                |        // lorem ipsum
                |    }
                |}
                |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `closing parenthesis bug`() {
        lintMethod(
            """
                    |fun foo() {
                    |    return x +
                    |            (y +
                    |                    foo(x)
                    |            )
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `should trigger on string templates starting with new line`() {
        lintMethod(
            """
                |fun foo(some: String) {
                |    fun bar() {
                |        val a = "${'$'}{
                |        expression
                |            .foo()
                |            .bar()
                |        }"
                |    }
                |
                |    val b = "${'$'}{ foo().bar() }"
                |}
                |
            """.trimMargin(),
            DiktatError(4, 1, ruleId, warnText(12, 8), true),
            DiktatError(5, 1, ruleId, warnText(16, 12), true),
            DiktatError(6, 1, ruleId, warnText(16, 12), true),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `check script`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                |val q = 1
                |
            """.trimMargin(),
            tempDir = tempDir,
            fileName = "src/main/kotlin/com/saveourtool/diktat/Example.kts"
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `check gradle script`(@TempDir tempDir: Path) {
        lintMethodWithFile(
            """
                |projectName = "diKTat"
                |
            """.trimMargin(),
            tempDir = tempDir,
            fileName = "src/main/kotlin/com/saveourtool/diktat/build.gradle.kts"
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `should warn message with configured indentation size`() {
        val rulesConfigListWithIndentation2 = rulesConfigList.map { ruleConfig ->
            ruleConfig.copy(configuration = ruleConfig.configuration.mapValues { (key, value) ->
                if (key == INDENTATION_SIZE) {
                    "2"
                } else {
                    value
                }
            })
        }
        val warnMessage = WRONG_INDENTATION.warnText().replace("4", "2")
        lintMethod(
            """
                |fun foo(some: String) {
                |    print("test")
                |if (test){
                |print("2")
                |}
                |}
                |
            """.trimMargin(),
            DiktatError(2, 1, ruleId, "$warnMessage expected 2 but was 4", true),
            DiktatError(3, 1, ruleId, "$warnMessage expected 2 but was 0", true),
            DiktatError(4, 1, ruleId, "$warnMessage expected 4 but was 0", true),
            DiktatError(5, 1, ruleId, "$warnMessage expected 2 but was 0", true),
            rulesConfigList = rulesConfigListWithIndentation2
        )
    }

    private fun warnText(expected: Int, actual: Int) = "${WRONG_INDENTATION.warnText()} expected $expected but was $actual"
}
