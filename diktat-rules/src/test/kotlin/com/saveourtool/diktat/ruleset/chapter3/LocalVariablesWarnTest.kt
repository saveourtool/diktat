package com.saveourtool.diktat.ruleset.chapter3

import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.rules.chapter3.identifiers.LocalVariablesRule
import com.saveourtool.diktat.util.LintTestBase

import com.saveourtool.diktat.api.DiktatError
import generated.WarningNames.LOCAL_VARIABLE_EARLY_DECLARATION
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Suppress("LargeClass")
class LocalVariablesWarnTest : LintTestBase(::LocalVariablesRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${LocalVariablesRule.NAME_ID}"

    private fun warnMessage(name: String,
                            declared: Int,
                            used: Int
    ) = "<$name> is declared on line <$declared> and is used for the first time on line <$used>"

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not check top-level and member properties`() {
        lintMethod(
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
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables used only in this scope - positive example`() {
        lintMethod(
            """
                    |import org.diktat.Some as test
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
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables used only in this scope - positive example with blank lines`() {
        lintMethod(
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
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables used only in this scope - positive example with comments`() {
        lintMethod(
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
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local var used only in this scope - positive example`() {
        lintMethod(
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
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local var used only in this scope with multiline usage - positive example`() {
        lintMethod(
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
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables used only in this scope`() {
        lintMethod(
            """
                    |fun foo() {
                    |    val bar = 0
                    |    println()
                    |    baz(bar)
                    |}
            """.trimMargin(),
            DiktatError(2, 5, ruleId, "${Warnings.LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} ${warnMessage("bar", 2, 4)}", false)
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables used only in this scope - multiline declaration, positive example`() {
        lintMethod(
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
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables used only in this scope - multiline declaration with binary expression`() {
        lintMethod(
            """
                    |fun foo() {
                    |    val bar = 1 +
                    |        2
                    |    println()
                    |    baz(bar)
                    |}
            """.trimMargin(),
            DiktatError(2, 5, ruleId, "${Warnings.LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} ${warnMessage("bar", 2, 5)}", false)
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables used only in this scope - multiline declaration with dot qualified property access`() {
        lintMethod(
            """
                    |fun foo() {
                    |    val bar = "string"
                    |        .size
                    |    println()
                    |    baz(bar)
                    |}
            """.trimMargin(),
            DiktatError(2, 5, ruleId, "${Warnings.LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} ${warnMessage("bar", 2, 5)}", false)
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables used only in this scope - multiline declaration with dot qualified method call`() {
        lintMethod(
            """
                    |fun foo() {
                    |    val bar = "string"
                    |        .count()
                    |    println()
                    |    baz(bar)
                    |}
            """.trimMargin(),
            DiktatError(2, 5, ruleId, "${Warnings.LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} ${warnMessage("bar", 2, 5)}", false)
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    @Disabled("Checking of variable from outer scope is not supported yet")
    fun `local variables defined in outer scope and used only in nested scope`() {
        lintMethod(
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
            DiktatError(2, 5, ruleId, "${Warnings.LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} val bar = 0", false)
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `local variables defined in outer scope and used in several scopes - positive example`() {
        lintMethod(
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
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    @Disabled("Checking of variable from outer scope is not supported yet")
    fun `local variables defined in outer scope and used in several scopes`() {
        lintMethod(
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
            DiktatError(2, 5, ruleId, "${Warnings.LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} val bar = 0", false)
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not trigger on other objects fields with same name`() {
        lintMethod(
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
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    @Disabled("Checking of variable from outer scope is not supported yet")
    fun `need to allow declaring vars outside of loops`() {
        lintMethod(
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
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `discovered during testing`() {
        lintMethod(
            """
                    |fun foo() {
                    |    var offset = 0
                    |    for (x in 1..100) {
                    |        offset += it.length
                    |    }
                    |    return offset
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    @Disabled("Checking of variable from outer scope is not supported yet")
    fun `need to allow declaring vars outside collection methods`() {
        lintMethod(
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
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not trigger on properties with same name in different scopes`() {
        lintMethod(
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
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not trigger on properties with same name in different scopes - 2`() {
        lintMethod(
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
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not trigger when variables from outer scope are shadowed by lambda parameters`() {
        lintMethod(
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
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should check usage inside lambdas - positive example`() {
        lintMethod(
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
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should check usage inside lambdas`() {
        lintMethod(
            """
                    |fun foo(): Bar {
                    |    val x = 0
                    |    println()
                    |    list.forEach {
                    |        bar(x)
                    |    }
                    |}
            """.trimMargin(),
            DiktatError(2, 5, ruleId, "${Warnings.LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} ${warnMessage("x", 2, 5)}", false)
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should check usage inside lambdas with line break`() {
        lintMethod(
            """
                    |fun foo(): Bar {
                    |    val x = 0
                    |    println()
                    |    list
                    |        .forEach {
                    |            bar(x)
                    |    }
                    |}
            """.trimMargin(),
            DiktatError(2, 5, ruleId, "${Warnings.LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} ${warnMessage("x", 2, 6)}", false)
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should check properties declared inside lambda`() {
        lintMethod(
            """
                    |fun foo() {
                    |    list.map {
                    |        val foo = "bar"
                    |        println()
                    |        foo.baz(it)
                    |    }
                    |}
            """.trimMargin(),
            DiktatError(3, 9, ruleId, "${Warnings.LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} ${warnMessage("foo", 3, 5)}", false)
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not trigger when more than one variables need to be declared`() {
        lintMethod(
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
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not raise warning if there is property not in propertyToUsages between other properties declaration`() {
        lintMethod(
            """
                    |fun foo() {
                    |    val x = 0
                    |    val a = -1
                    |    val y = 1
                    |    val z = 2
                    |    foo(x, y, z)
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not trigger on properties`() {
        lintMethod(
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
            """.trimMargin()
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    @Disabled("Constructors are not handled separately yet")
    fun `should check variables initialized with constructor with no parameters`() {
        lintMethod(
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

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `check properties initialized with some selected methods`() {
        lintMethod(
            """
                    |fun foo() {
                    |    val list = emptyList<Int>()
                    |    println()
                    |    bar(list)
                    |}
            """.trimMargin(),
            DiktatError(2, 5, ruleId, "${Warnings.LOCAL_VARIABLE_EARLY_DECLARATION.warnText()} ${warnMessage("list", 2, 4)}", false)
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should properly detect containing scope of lambdas`() {
        lintMethod(
            """
                    |fun foo() {
                    |    val res = mutableListOf<Type>()
                    |    Foo.bar(
                    |            Foo.baz(
                    |                    cb = { e, _ -> res.add(e) }
                    |            )
                    |    )
                    |    Assertions.assertThat(res).isEmpty()
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not trigger when there is a property in a class and same variable name in function and lambda`() {
        lintMethod(
            """
                    |class Example {
                    |    val a = "a1"
                    |    fun foo() {
                    |        val a = "a2"
                    |        listOf<String>().forEach { a -> println(a) }
                    |    }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not trigger on triple quoted strings`() {
        lintMethod(
            """
                    |class Example {
                    |    fun some() {
                    |       val code = ${"\"\"\""}
                    |                 class Some {
                    |                   fun for() : String {
                    |                   }
                    |                 }
                    |               ${"\"\"\""}.trimIndent()
                    |       bar(code)
                    |    }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not trigger on space after last val`() {
        lintMethod(
            """
                    |    private fun collectAllExtensionFunctions(node: ASTNode): SimilarSignatures {
                    |       val extensionFunctionList = node.findAllNodesWithSpecificType(FUN).filter { it.hasChildOfType(TYPE_REFERENCE) && it.hasChildOfType(DOT) }
                    |       val distinctFunctionSignatures = mutableMapOf<FunctionSignature, ASTNode>()  // maps function signatures on node it is used by
                    |       val extensionFunctionsPairs = mutableListOf<Pair<ExtensionFunction, ExtensionFunction>>()  // pairs extension functions with same signature
                    |
                    |       extensionFunctionList.forEach { func ->
                    |           if (distinctFunctionSignatures.contains(signature)) {
                    |               val secondFuncClassName = distinctFunctionSignatures[signature]!!.findChildBefore(DOT, TYPE_REFERENCE)!!.text
                    |               extensionFunctionsPairs.add(Pair(
                    |               ExtensionFunction(secondFuncClassName, signature, distinctFunctionSignatures[signature]!!),
                    |               ExtensionFunction(className, signature, func)))
                    |           } else {
                    |               distinctFunctionSignatures[signature] = func
                    |           }
                    |       }
                    |       return extensionFunctionsPairs
                    |   }
            """.trimMargin()
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should not trigger on var nodes which have initializer`() {
        lintMethod(
            """
                    |    private fun collectAllExtensionFunctions(astNode: ASTNode): SimilarSignatures {
                    |       var text = ""
                    |       var node = astNode
                    |       var prevNode: ASTNode
                    |       do {
                    |           prevNode = node
                    |           node = node.treeParent
                    |           if (node.elementType == ElementType.PARENTHESIZED) {
                    |               text += getTextFromParenthesized(node)
                    |           }
                    |       } while (node.elementType != BINARY_EXPRESSION)
                    |   }
            """.trimMargin()
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should skip comments`() {
        lintMethod(
            """
                    |    private fun collectAllExtensionFunctions(astNode: ASTNode): SimilarSignatures {
                    |       var copyrightComment = ""
                    |       var headerKdoc = listOf()
                    |       // Annotations with target`file` can only be placed before `package` directive.
                    |       var fileAnnotations = node.findChildByType(FILE_ANNOTATION_LIST)
                    |       // We also collect all other elements that are placed on top of the file.
                    |       // These may be other comments, so we just place them before the code starts.
                    |       val otherNodesBeforeCode = firstCodeNode.siblings(forward = false)
                    |           .filterNot {
                    |               it.isWhiteSpace() ||
                    |                   it == copyrightComment || it == headerKdoc || it == fileAnnotations
                    |           }
                    |           .toList()
                    |           .reversed()
                    |   }
            """.trimMargin()
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should skip val nodes between considered nodes`() {
        lintMethod(
            """
                    |    private fun collectAllExtensionFunctions(astNode: ASTNode): SimilarSignatures {
                    |       val text = ""
                    |       val node = astNode
                    |       var prevNode: ASTNode
                    |       some(text, node, prevNode)
                    |   }
            """.trimMargin()
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `should skip val and var nodes between considered nodes`() {
        // Only text goes to propertyToUsages here
        lintMethod(
            """
                    |    private fun collectAllExtensionFunctions(astNode: ASTNode): SimilarSignatures {
                    |       val text = ""
                    |       val node = astNode
                    |       val prevNode: ASTNode = astNode
                    |       some(text, node, prevNode)
                    |   }
            """.trimMargin()
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `shouldn't fail on double invocations`() {
        lintMethod(
            """
                |fun bar() {
                |    val x = foo()()
                |    val x = foo()()()
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(LOCAL_VARIABLE_EARLY_DECLARATION)
    fun `shouldn't fail on semicolon`() {
        lintMethod(
            """
                |fun bar() {
                |    var a = 0;
                |    a++
                |}
            """.trimMargin()
        )
    }
}
