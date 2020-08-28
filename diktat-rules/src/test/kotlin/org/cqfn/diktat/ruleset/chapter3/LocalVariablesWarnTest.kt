package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings.LOCAL_VARIABLE_EARLY_DECLARATION
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.identifiers.LocalVariablesRule
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class LocalVariablesWarnTest {
    private val ruleId = "$DIKTAT_RULE_SET_ID:local-variables"

    private fun warnMessage(name: String, declared: Int, used: Int) = "$name is declared on line $declared and used for the first time on line $used"

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not check top-level and member properties`() {
        lintMethod(LocalVariablesRule(),
                """
                    |const val foo = 0
                    |
                    |class Example {
                    |    val bar = 0
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables used only in this scope - positive example`() {
        lintMethod(LocalVariablesRule(),
                """
                    |class Example {
                    |    fun foo() {
                    |        val bar = 0
                    |        baz(bar)
                    |        println()
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables used only in this scope - positive example with blank lines`() {
        lintMethod(LocalVariablesRule(),
                """
                    |class Example {
                    |    fun foo() {
                    |        val bar = 0
                    |        
                    |        baz(bar)
                    |        println()
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables used only in this scope - positive example with comments`() {
        lintMethod(LocalVariablesRule(),
                """
                    |class Example {
                    |    fun foo() {
                    |        val bar = 0
                    |        // comment
                    |        baz(bar)
                    |        println()
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local var used only in this scope - positive example`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo() {
                    |    var bar: MutableList<Int>
                    |    baz(bar)
                    |    println()
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local var used only in this scope with multiline usage - positive example`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo(obj: Type?) {
                    |    var bar: MutableList<Int>
                    |    obj
                    |        ?.baz(bar)
                    |    println()
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables used only in this scope`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo() {
                    |    val bar = 0
                    |    println()
                    |    baz(bar)
                    |}
                """.trimMargin(),
                LintError(2, 5, ruleId, "${LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} ${warnMessage("bar", 2, 4)}", false)
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables used only in this scope - multiline declaration, positive example`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo() {
                    |    val bar = obj
                    |        .foo()
                    |    baz(bar)
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables used only in this scope - multiline declaration with binary expression`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo() {
                    |    val bar = 1 + 
                    |        2
                    |    println()
                    |    baz(bar)
                    |}
                """.trimMargin(),
                LintError(2, 5, ruleId, "${LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} ${warnMessage("bar", 2, 5)}", false)
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables used only in this scope - multiline declaration with dot qualified property access`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo() {
                    |    val bar = "string"
                    |        .size
                    |    println()
                    |    baz(bar)
                    |}
                """.trimMargin(),
                LintError(2, 5, ruleId, "${LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} ${warnMessage("bar", 2, 5)}", false)
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables used only in this scope - multiline declaration with dot qualified method call`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo() {
                    |    val bar = "string"
                    |        .count()
                    |    println()
                    |    baz(bar)
                    |}
                """.trimMargin(),
                LintError(2, 5, ruleId, "${LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} ${warnMessage("bar", 2, 5)}", false)
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    @Disabled("Checking of variable from outer scope is not supported yet")
    fun `local variables defined in outer scope and used only in nested scope`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo() {
                    |    val bar = 0
                    |    try {
                    |        baz(bar)
                    |    } catch (e: Exception) {
                    |        println()
                    |    }
                    |}
                """.trimMargin(),
                LintError(2, 5, ruleId, "${LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} val bar = 0", false)
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables defined in outer scope and used in several scopes - positive example`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo() {
                    |    val bar = 0
                    |    try {
                    |        baz(bar)
                    |        println()
                    |    } catch (e: Exception) {
                    |        qux(bar)
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    @Disabled("Checking of variable from outer scope is not supported yet")
    fun `local variables defined in outer scope and used in several scopes`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo() {
                    |    val bar = 0
                    |    if (condition) {
                    |        try {
                    |            baz(bar)
                    |            println()
                    |        } catch (e: Exception) {
                    |            qux(bar)
                    |        }
                    |    } else {
                    |        println()
                    |    }
                    |}
                """.trimMargin(),
                LintError(2, 5, ruleId, "${LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} val bar = 0", false)
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not trigger on other objects fields with same name`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo() {
                    |    val size = list.size
                    |    if (size > maxSize) {
                    |        bar()
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    @Disabled("Checking of variable from outer scope is not supported yet")
    fun `need to allow declaring vars outside of loops`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo() {
                    |    var offset = 0
                    |    for (x in 1..100) {
                    |        if (condition) {
                    |            bar(offset)
                    |        }
                    |        offset += it.length
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    @Disabled("Checking of variable from outer scope is not supported yet")
    fun `need to allow declaring vars outside collection methods`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo() {
                    |    var offset = 0
                    |    list.forEach {
                    |        if (condition) {
                    |            bar(offset)
                    |        }
                    |        offset += it.length
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not trigger on properties with same name in different scopes`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo(): Bar {
                    |    if (condition) {
                    |        val x = bar()
                    |        return Bar(x)
                    |    } else {
                    |        val x = baz()
                    |        return Bar(x)
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not trigger on properties with same name in different scopes - 2`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo(): Bar {
                    |    for (x in A) {
                    |        val y = bar()
                    |        qux(y)
                    |    }
                    |    val y = bar()
                    |    qux(y)
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not trigger when variables from outer scope are shadowed by lambda parameters`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo(): Bar {
                    |    val x = 0
                    |    list.forEach { x ->
                    |        println()
                    |        bar(x)
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should check usage inside lambdas - positive example`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo(): Bar {
                    |    val x = 0
                    |    list.forEach {
                    |        bar(x)
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should check usage inside lambdas`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo(): Bar {
                    |    val x = 0
                    |    println()
                    |    list.forEach {
                    |        bar(x)
                    |    }
                    |}
                """.trimMargin(),
                LintError(2, 5, ruleId, "${LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} ${warnMessage("x", 2, 4)}", false)
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not trigger when more than one variables need to be declared`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo() {
                    |    val x = 0
                    |    val y = 1
                    |    foo(x, y)
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should check when more than one variables need to be declared`() {
        lintMethod(LocalVariablesRule(),
                """
                    |fun foo() {
                    |    val x = 0
                    |    val a = -1
                    |    val y = 1
                    |    val z = 2
                    |    foo(x, y, z)
                    |}
                """.trimMargin(),
                LintError(2, 5, ruleId, "${LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} ${warnMessage("x", 2, 6)}", false)
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should emit only one warning when same variables are used more than once`() {
        lintMethod(LocalVariablesRule(),
                """
                    |private fun checkDoc(node: ASTNode, warning: Warnings) {
                    |    val a = 0
                    |    val b = 1
                    |    val c = 2
                    |
                    |    if (predicate(a) && predicate(b)) {
                    |        foo(a, b, c)
                    |    }
                    |}
                """.trimMargin(),
                LintError(2, 5, ruleId, "${LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} ${warnMessage("a", 2, 6)}", false),
                LintError(3, 5, ruleId, "${LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} ${warnMessage("b", 3, 6)}", false)
        )
    }

    @Test
    @Tag(WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION)
    @Disabled("Constructors are not handled separately yet")
    fun `should check variables initialized with constructor with no parameters`() {
        lintMethod(LocalVariablesRule(),
                """
                     |fun foo(isRequired: Boolean): Type {
                     |    val resOption = Type()
                     |    println()
                     |    resOption.isRequired = isRequired
                     |    return resOption
                     |}
                     """.trimMargin()
        )
    }
}
