package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.COMMA
import com.pinterest.ktlint.core.ast.ElementType.ENUM_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.SEMICOLON
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isWhiteSpaceWithNewline
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.ENUMS_SEPARATED
import org.cqfn.diktat.ruleset.utils.appendNewlineMergingWhiteSpace
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.cqfn.diktat.ruleset.utils.isClassEnum
import org.cqfn.diktat.ruleset.utils.allSiblings
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl

class EnumsSeparated : Rule("enum-separated") {

    companion object {
        private val SIMPLE_VALUE = listOf(IDENTIFIER, WHITE_SPACE, COMMA, SEMICOLON)
        private val SIMPLE_ENUM = listOf(ENUM_ENTRY, WHITE_SPACE, LBRACE, RBRACE)
    }

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

        if (node.elementType == CLASS && node.hasChildOfType(CLASS_BODY))
            if (node.isClassEnum())
                checkEnumEntry(node)
    }

    //Fixme prefer enum classes if it is possible instead of variables
    private fun checkEnumEntry(node: ASTNode) {
        val enumEntries = node.findChildByType(CLASS_BODY)!!.getAllChildrenWithType(ENUM_ENTRY)
        if (enumEntries.isEmpty() || (isEnumSimple(enumEntries) && isEnumOneLine(enumEntries)))
            return
        enumEntries.forEach {
            if (!it.treeNext.isWhiteSpaceWithNewline())
                ENUMS_SEPARATED.warnAndFix(configRules, emitWarn, isFixMode, "enum entries must end with a line break",
                        it.startOffset) {
                    it.appendNewlineMergingWhiteSpace(it.treeNext, it.treeNext)
                }
        }
        checkLastEnum(enumEntries.last())
    }

    private fun isEnumOneLine(nodes: List<ASTNode>) =
            nodes.dropLast(1).none { it.treeNext.isWhiteSpaceWithNewline() }

    private fun isEnumSimple(enumEntries: List<ASTNode>): Boolean {
        enumEntries.forEach { node ->
            if (!SIMPLE_VALUE.containsAll(node.getChildren(null).map { it.elementType }))
                return false
        }
        return SIMPLE_ENUM.containsAll(enumEntries.last().allSiblings(withSelf = true).map { it.elementType })
    }

    private fun checkLastEnum(node: ASTNode) {
        if (!node.hasChildOfType(SEMICOLON)) {
            ENUMS_SEPARATED.warnAndFix(configRules, emitWarn, isFixMode, "enums must end with semicolon",
                    node.startOffset) {
                node.addChild(LeafPsiElement(SEMICOLON, ";"), null)
                node.addChild(PsiWhiteSpaceImpl("\n"), node.findChildByType(SEMICOLON)!!)
            }
        } else if (!node.findChildByType(SEMICOLON)!!.treePrev.isWhiteSpaceWithNewline()) {
            ENUMS_SEPARATED.warnAndFix(configRules, emitWarn, isFixMode, "semicolon must be on a new line",
                    node.startOffset) {
                node.appendNewlineMergingWhiteSpace(node.findChildByType(SEMICOLON)!!, node.findChildByType(SEMICOLON)!!)
            }
        }
        if (!node.hasChildOfType(COMMA)) {
            ENUMS_SEPARATED.warnAndFix(configRules, emitWarn, isFixMode, "last enum entry must end with a comma",
                    node.startOffset) {
                node.addChild(LeafPsiElement(COMMA, ","), node.findChildByType(SEMICOLON)!!.treePrev)
            }
        }
    }
}
