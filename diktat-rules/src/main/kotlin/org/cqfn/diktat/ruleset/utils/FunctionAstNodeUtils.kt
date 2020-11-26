package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.CONSTRUCTOR_CALLEE
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.OVERRIDE_KEYWORD
import org.cqfn.diktat.ruleset.rules.PackageNaming.Companion.PACKAGE_PATH_ANCHOR
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtParameterList

fun ASTNode.hasParameters(): Boolean {
    checkNodeIsFun(this)
    val argList = this.argList()
    return argList != null && argList.hasChildOfType(ElementType.VALUE_PARAMETER)
}

fun ASTNode.parameterNames(): Collection<String?> {
    checkNodeIsFun(this)
    return (psi as KtFunction).valueParameters.map { it.name }
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
            functionName.text.startsWith(SET_PREFIX) -> parameterNames().size == 1
            functionName.text.startsWith(GET_PREFIX) -> parameterNames().isEmpty()
            else -> false
        }
    } ?: false
}

/**
 * Check whether this function is a standard method
 */
fun ASTNode.isStandardMethod() = also(::checkNodeIsFun)
        .getIdentifierName()
        ?.let { it.text in standardMethods }
        ?: false


fun ASTNode.isOverridden() : Boolean =
        getFirstChildWithType(MODIFIER_LIST)
        ?.hasChildOfType(OVERRIDE_KEYWORD) ?: false


private fun ASTNode.argList(): ASTNode? {
    checkNodeIsFun(this)
    return this.getFirstChildWithType(ElementType.VALUE_PARAMETER_LIST)
}

private fun checkNodeIsFun(node: ASTNode) =
        require(node.elementType == ElementType.FUN) {
            "This utility method operates on nodes of type ElementType.FUN only"
        }
