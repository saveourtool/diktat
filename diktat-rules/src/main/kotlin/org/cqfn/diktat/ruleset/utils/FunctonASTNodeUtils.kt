package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.CONSTRUCTOR_CALLEE
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import org.cqfn.diktat.ruleset.rules.PackageNaming.Companion.PACKAGE_PATH_ANCHOR
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtParameterList

private val getPrefix = "get"
private val setPrefix = "set"

/**
 * Checks whether function from this [ElementType.FUN] node has `@Test` annotation
 */
fun ASTNode.hasTestAnnotation(): Boolean {
    checkNodeIsFun(this)
    return findChildByType(MODIFIER_LIST)
            ?.getAllChildrenWithType(ANNOTATION_ENTRY)
            ?.flatMap { it.findAllNodesWithSpecificType(CONSTRUCTOR_CALLEE) }
            ?.any { it.findLeafWithSpecificType(IDENTIFIER)?.text == "Test" }
            ?: false
}

/**
 * Checks whether function from this [ElementType.FUN] node is located in file src/test/**/*Test.kt
 * @param testAnchors names of test directories, e.g. "test", "jvmTest"
 */
fun ASTNode.isLocatedInTest(filePathParts: List<String>, testAnchors: List<String>): Boolean {
    checkNodeIsFun(this)
    return filePathParts
            .takeIf { it.contains(PACKAGE_PATH_ANCHOR) }
            ?.run { subList(lastIndexOf(PACKAGE_PATH_ANCHOR), size) }
            ?.run {
                // e.g. src/test/ClassTest.kt, other files like src/test/Utils.kt are still checked
                testAnchors.any { contains(it) } && last().substringBeforeLast('.').endsWith("Test")
            }
            ?: false
}

fun ASTNode.hasParameters(): Boolean {
    checkNodeIsFun(this)
    val argList = this.argList()
    return argList != null && argList.hasChildOfType(ElementType.VALUE_PARAMETER)
}

fun ASTNode.parameterNames(): Collection<String?>? {
    checkNodeIsFun(this)
    return (this.argList()?.psi as KtParameterList?)
            ?.parameters?.map { (it as KtParameter).name }
}

/**
 * Returns list of lines of this function body, excluding opening and closing braces if they are on separate lines
 */
fun ASTNode.getBodyLines(): List<String> {
    checkNodeIsFun(this)
    return this.getFirstChildWithType(BLOCK)?.let { blockNode ->
        blockNode.text.lines()
                .let { if (it.first().matches("\\{\\s*".toRegex())) it.drop(1) else it }
                .let { if (it.last().matches("\\s*}".toRegex())) it.dropLast(1) else it }
    } ?: emptyList()
}

/**
 * Checks if this function is getter or setter according to it's signature
 */
fun ASTNode.isGetterOrSetter(): Boolean {
    checkNodeIsFun(this)
    return getIdentifierName()?.let { functionName ->
        when {
            functionName.text.startsWith(setPrefix) -> parameterNames()!!.size == 1
            functionName.text.startsWith(getPrefix) -> parameterNames()!!.isEmpty()
            else -> false
        }
    } ?: false
}

private fun ASTNode.argList(): ASTNode? {
    checkNodeIsFun(this)
    return this.getFirstChildWithType(ElementType.VALUE_PARAMETER_LIST)
}

private fun checkNodeIsFun(node: ASTNode) =
        require(node.elementType == ElementType.FUN) {
            "This utility method operates on nodes of type ElementType.FUN only"
        }
