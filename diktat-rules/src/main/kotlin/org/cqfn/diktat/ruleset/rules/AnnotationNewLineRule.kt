package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.FUN_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.PRIMARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.SECONDARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.isWhiteSpace
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import java.lang.StringBuilder

/**
 * This rule makes each annotation applied to a class, method or constructor is on its own line. Except: if first annotation of constructor
 */
class AnnotationNewLineRule : Rule("annotation-new-line") {

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.getDiktatConfigRules()
        emitWarn = emit
        isFixMode = autoCorrect

        when (node.elementType) {
            CLASS, FUN, PRIMARY_CONSTRUCTOR, SECONDARY_CONSTRUCTOR -> checkAnnotation(node)
        }
    }


    private fun checkAnnotation(node: ASTNode) {
        if (node.hasChildOfType(MODIFIER_LIST)) {
            val modList = node.findChildByType(MODIFIER_LIST)
            fixAnnotation(modList!!)
        }
    }

    private fun fixAnnotation(node: ASTNode) {
        if (node.getAllChildrenWithType(ANNOTATION_ENTRY).size <= 1)
            return

        node.getAllChildrenWithType(ANNOTATION_ENTRY).forEach {
            
            if (!it.isFollowedByNewline() && !it.isBeginByNewline()) {
                deleteSpaces(it)
            } else if (!it.isFollowedByNewline()) {
                deleteSpaces(it, side = Side.RIGHT)
            } else if (!it.isBeginByNewline()) {
                deleteSpaces(it, side = Side.LEFT)
            }
        }

    }

    private fun deleteSpaces(node: ASTNode, side: Side = Side.BOTH) {
        Warnings.ANNOTATION_NEW_LINE.warnAndFix(configRules, emitWarn, isFixMode, "${node.text} not on a single line",
                node.startOffset) {
            if ((side == Side.BOTH || side == Side.RIGHT)) {
                if (node.treeNext != null) {
                    if (node.treeNext.isWhiteSpace()) {
                        node.removeChild(node.treeNext)
                    }
                    node.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node.treeNext)
                } else {
                    node.treeParent.addChild(PsiWhiteSpaceImpl("\n"), null)
                }
            }

            if((side == Side.BOTH || side == Side.LEFT)) {
                if (node.treePrev != null) {
                    if (node.treePrev.isWhiteSpace()) {
                        node.removeChild(node.treePrev)
                    }
                    node.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node.treePrev)
                } else {
                    node.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node)
                }
            }
        }
    }

    private enum class Side {
        RIGHT, LEFT, BOTH
    }

}
