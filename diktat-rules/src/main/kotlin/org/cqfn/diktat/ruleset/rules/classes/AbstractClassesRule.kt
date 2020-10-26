package org.cqfn.diktat.ruleset.rules.classes

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.ABSTRACT_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
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

            if (checkAbstractModifier(node)) {
                handleAbstractClass(classBody, node)
            }
        }
    }

    private fun checkAbstractModifier(node: ASTNode): Boolean =
            node.getFirstChildWithType(MODIFIER_LIST)?.hasChildOfType(ABSTRACT_KEYWORD) ?: false

    private fun handleAbstractClass(node: ASTNode, classNode: ASTNode) {
        val functions = node.getAllChildrenWithType(FUN)

        val identifier = classNode.getFirstChildWithType(IDENTIFIER)!!.text

        if (functions.isNotEmpty() && functions.none { checkAbstractModifier(it) }) {
            CLASS_SHOULD_NOT_BE_ABSTRACT.warnAndFix(configRule, emitWarn, isFixMode, identifier, node.startOffset, node) {
                classNode.removeChild(classNode.getFirstChildWithType(MODIFIER_LIST)!!)
                if (classNode.firstChildNode.isWhiteSpace()) {
                    classNode.removeChild(classNode.firstChildNode)
                }
            }
        }
    }
}
