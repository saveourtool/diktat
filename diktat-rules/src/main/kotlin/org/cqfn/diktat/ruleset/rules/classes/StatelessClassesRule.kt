package org.cqfn.diktat.ruleset.rules.classes

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.INTERFACE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.OBJECT_DECLARATION
import com.pinterest.ktlint.core.ast.ElementType.OBJECT_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.SUPER_TYPE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.SUPER_TYPE_LIST
import com.pinterest.ktlint.core.ast.children
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getAllLeafsWithSpecificType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.psi.KtClass

/**
 * This rule checks if class is stateless and if so changes it to object.
 */
class StatelessClassesRule(private val configRule: List<RulesConfig>) : Rule("stateless-class") {
    private lateinit var emitWarn: EmitType
    private var isFixMode: Boolean = false

    override fun visit(
            node: ASTNode,
            autoCorrect: Boolean,
            emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        emitWarn = emit
        isFixMode = autoCorrect

        // Fixme: We should find interfaces in all project and then check them
        if (node.elementType == FILE) {
            val interfacesNodes = node
                    .findAllNodesWithSpecificType(CLASS)
                    .filter { it.hasChildOfType(INTERFACE_KEYWORD) }
            node
                    .findAllNodesWithSpecificType(CLASS)
                    .filterNot { it.hasChildOfType(INTERFACE_KEYWORD) }
                    .forEach{handleClass(it, interfacesNodes)}
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleClass(node: ASTNode, interfaces: List<ASTNode>) {
        if (isClassExtendsValidInterface(node, interfaces) && isStatelessClass(node)) {
            Warnings.OBJECT_IS_PREFERRED.warnAndFix(configRule, emitWarn, isFixMode,
                    "class ${(node.psi as KtClass).name!!}", node.startOffset, node) {
                val newObjectNode = CompositeElement(OBJECT_DECLARATION)
                node.treeParent.addChild(newObjectNode, null)
                node.children().forEach {
                    newObjectNode.addChild(it.copyElement(), null)
                }
                newObjectNode.addChild(LeafPsiElement(OBJECT_KEYWORD, "object"),
                        newObjectNode.getFirstChildWithType(CLASS_KEYWORD))
                newObjectNode.removeChild(newObjectNode.getFirstChildWithType(CLASS_KEYWORD)!!)
                node.treeParent.removeChild(node)
            }
        }
    }

    private fun isStatelessClass(node: ASTNode): Boolean {
        val properties = (node.psi as KtClass).getProperties()
        val functions = node.findAllNodesWithSpecificType(FUN)
        return properties.isNullOrEmpty()
                && functions.isNotEmpty()
                && !(node.psi as KtClass).hasExplicitPrimaryConstructor()
    }

    private fun isClassExtendsValidInterface(node: ASTNode, interfaces: List<ASTNode>) : Boolean =
            node.findChildByType(SUPER_TYPE_LIST)
                ?.getAllChildrenWithType(SUPER_TYPE_ENTRY)
                ?.isNotEmpty()
                ?.and(classInheritsStatelessInterface(node, interfaces))
                    ?: false

    @Suppress("UnsafeCallOnNullableType")
    private fun classInheritsStatelessInterface (node: ASTNode, interfaces: List<ASTNode>): Boolean {
        val classInterfaces = node
                .findChildByType(SUPER_TYPE_LIST)
                ?.getAllChildrenWithType(SUPER_TYPE_ENTRY)

        val foundInterfaces = interfaces.filter { inter ->
            classInterfaces!!.any { it.text == inter.getFirstChildWithType(IDENTIFIER)!!.text }
        }

        return foundInterfaces.any { (it.psi as KtClass).getProperties().isEmpty() }
    }
}
