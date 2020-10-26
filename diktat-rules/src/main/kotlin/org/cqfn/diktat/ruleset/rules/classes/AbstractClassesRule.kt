package org.cqfn.diktat.ruleset.rules.classes

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.ABSTRACT_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isWhiteSpace
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.CLASS_SHOULD_NOT_BE_ABSTRACT
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Checks if abstract class has any abstract method. If not, warns that class should not be abstract
 */
class AbstractClassesRule(private val configRule: List<RulesConfig>) : Rule("abstract-classes") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
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
                    // we are deleting one keyword, so we need to delete extra space
                    val spaceInModifiers = modList.getFirstChildWithType(WHITE_SPACE)
                    modList.removeChild(modList.getFirstChildWithType(ABSTRACT_KEYWORD)!!)
                    if (spaceInModifiers != null && spaceInModifiers.isWhiteSpace()) {
                        modList.removeChild(spaceInModifiers)
                    }
                } else {
                    classNode.removeChild(modList)
                    if (classNode.firstChildNode.isWhiteSpace()) {
                        classNode.removeChild(classNode.firstChildNode)
                    }
                }
            }
        }
    }
}
