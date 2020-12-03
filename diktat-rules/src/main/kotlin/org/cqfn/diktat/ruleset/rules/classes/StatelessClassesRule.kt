package org.cqfn.diktat.ruleset.rules.classes

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.OBJECT_DECLARATION
import com.pinterest.ktlint.core.ast.ElementType.OBJECT_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.SUPER_TYPE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.SUPER_TYPE_LIST
import com.pinterest.ktlint.core.ast.children
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.isPublic

/**
 * This rule checks if class is stateless and if so changes it to object.
 */
class StatelessClassesRule(private val configRule: List<RulesConfig>) : Rule("stateless-class") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(
            node: ASTNode,
            autoCorrect: Boolean,
            emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == CLASS) {
            handleClass(node)
        }
    }

    private fun handleClass(node: ASTNode) {
        if (isStatelessClass(node) && isClassExtendsInterface(node)) {
            Warnings.OBJECT_IS_PREFERRED.warnAndFix(configRule, emitWarn, isFixMode,
                    "class ${(node.psi as KtClass).name!!}", node.startOffset, node) {
                val newObjectNode = CompositeElement(OBJECT_DECLARATION)
                node.treeParent.addChild(newObjectNode, null)
                node.removeChild(node.firstChildNode)
                newObjectNode.addChild(LeafPsiElement(OBJECT_KEYWORD, "object"), null)
                node.children().forEach {
                    newObjectNode.addChild(it.copyElement(), null)
                }
                node.treeParent.removeChild(node)
            }
        }
    }

    private fun isStatelessClass(node: ASTNode): Boolean {
        val properties = (node.psi as KtClass).getProperties().filter { it.isVar && it.isPublic }
        val functions = node.findAllNodesWithSpecificType(FUN)
        return properties.isNullOrEmpty() && functions.isNotEmpty() && !(node.psi as KtClass).hasExplicitPrimaryConstructor()
    }

    private fun isClassExtendsInterface(node: ASTNode) : Boolean =
        node.findChildByType(SUPER_TYPE_LIST)?.getAllChildrenWithType(SUPER_TYPE_ENTRY)?.isNotEmpty() ?: false
}
