package org.cqfn.diktat.ruleset.rules.chapter6

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.prevSibling
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFunction

/**
 * This rule checks if there are any extension functions for the class in the same file, where it is defined
 */
class ExtensionFunctionsInFileRule(private val configRules: List<RulesConfig>) : Rule("extension-functions-class-file") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == ElementType.FILE) {
            val classNames = collectAllClassNames(node)

            collectAllExtensionFunctionsWithSameClassName(node, classNames).forEach {
                fireWarning(it)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun collectAllClassNames(node: ASTNode): List<String> {
        val classes = node.findAllNodesWithSpecificType(CLASS)

        return classes.map { (it.psi as KtClass).name!! }
    }

    private fun fireWarning(node: ASTNode) {
        Warnings.EXTENSION_FUNCTION_WITH_CLASS.warn(configRules, emitWarn, isFixMode, "fun ${(node.psi as KtFunction).name}", node.startOffset, node)
    }

    private fun collectAllExtensionFunctionsWithSameClassName(node: ASTNode, classNames: List<String>): List<ASTNode> =
            node.getAllChildrenWithType(FUN).filter { isExtensionFunctionWithClassName(it, classNames) }


    @Suppress("UnsafeCallOnNullableType")
    private fun isExtensionFunctionWithClassName(node: ASTNode, classNames: List<String>): Boolean =
            node.getFirstChildWithType(IDENTIFIER)!!.prevSibling { it.elementType == TYPE_REFERENCE }?.text in classNames
}
