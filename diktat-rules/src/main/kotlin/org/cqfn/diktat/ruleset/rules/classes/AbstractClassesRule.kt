package org.cqfn.diktat.ruleset.rules.classes

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.CLASS_SHOULD_NOT_BE_ABSTRACT
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.ABSTRACT_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.isWhiteSpace
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Checks if abstract class has any abstract method. If not, warns that class should not be abstract
 */
class AbstractClassesRule(private val configRule: List<RulesConfig>) : Rule("abstract-classes") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == CLASS) {
            val classBody = node.getFirstChildWithType(CLASS_BODY) ?: return

            if (hasAbstractModifier(node)) {
                handleAbstractClass(classBody, node)
            }
        }
    }

    private fun hasAbstractModifier(node: ASTNode): Boolean =
            node.getFirstChildWithType(MODIFIER_LIST)?.hasChildOfType(ABSTRACT_KEYWORD) ?: false

    @Suppress("UnsafeCallOnNullableType")
    private fun handleAbstractClass(node: ASTNode, classNode: ASTNode) {
        val functions = node.getAllChildrenWithType(FUN)

        val identifier = classNode.getFirstChildWithType(IDENTIFIER)!!.text

        if (functions.isNotEmpty() && functions.none { hasAbstractModifier(it) }) {
            CLASS_SHOULD_NOT_BE_ABSTRACT.warnAndFix(configRule, emitWarn, isFixMode, identifier, node.startOffset, node) {
                val modList = classNode.getFirstChildWithType(MODIFIER_LIST)!!
                if (modList.getChildren(null).size > 1) {
                    val abstractKeyword = modList.getFirstChildWithType(ABSTRACT_KEYWORD)!!

                    // we are deleting one keyword, so we need to delete extra space
                    val spaceInModifiers = if (abstractKeyword == modList.firstChildNode) {
                        abstractKeyword.treeNext
                    } else {
                        abstractKeyword.treePrev
                    }
                    modList.removeChild(abstractKeyword)
                    if (spaceInModifiers != null && spaceInModifiers.isWhiteSpace()) {
                        modList.removeChild(spaceInModifiers)
                    }
                } else {
                    if (modList.treeNext.isWhiteSpace()) {
                        classNode.removeChild(modList.treeNext)
                    }
                    classNode.removeChild(modList)
                }
            }
        }
    }
}
