/**
 * Various utility methods to work with AST nodes containing functions
 */

package com.saveourtool.diktat.ruleset.utils

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.KtNodeTypes.BLOCK
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction

/**
 * @return whether the function has any parameters
 */
fun ASTNode.hasParameters(): Boolean {
    checkNodeIsFun(this)
    val argList = this.argList()
    return argList != null && argList.hasChildOfType(KtNodeTypes.VALUE_PARAMETER)
}

/**
 * @return names of function parameters as Strings
 */
fun ASTNode.parameterNames(): Collection<String?> {
    checkNodeIsFun(this)
    return (psi as KtFunction).valueParameters.map { it.name }
}

/**
 * Returns list of lines of this function body, excluding opening and closing braces if they are on separate lines
 *
 * @return function body text as a list of strings
 */
fun ASTNode.getBodyLines(): List<String> {
    checkNodeIsFun(this)
    return this.getFirstChildWithType(BLOCK)?.let { blockNode ->
        blockNode.text
            .lines()
            .let { if (it.first().matches("\\{\\s*".toRegex())) it.drop(1) else it }
            .let { if (it.last().matches("\\s*}".toRegex())) it.dropLast(1) else it }
    } ?: emptyList()
}

/**
 * @return if this function is getter or setter according to it's signature
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
 * @return whether this function is a standard method
 */
fun ASTNode.isStandardMethod() = also(::checkNodeIsFun)
    .getIdentifierName()
    ?.let { it.text in standardMethods }
    ?: false

private fun ASTNode.argList(): ASTNode? {
    checkNodeIsFun(this)
    return this.getFirstChildWithType(KtNodeTypes.VALUE_PARAMETER_LIST)
}

private fun checkNodeIsFun(node: ASTNode) =
    require(node.elementType == KtNodeTypes.FUN) {
        "This utility method operates on nodes of type ElementType.FUN only"
    }
