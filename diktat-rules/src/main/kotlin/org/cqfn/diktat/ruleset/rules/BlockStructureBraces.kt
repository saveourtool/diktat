package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.BODY
import com.pinterest.ktlint.core.ast.ElementType.ELSE
import com.pinterest.ktlint.core.ast.ElementType.FOR
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.THEN
import com.pinterest.ktlint.core.ast.ElementType.WHEN
import com.pinterest.ktlint.core.ast.ElementType.WHEN_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.prevSibling
import org.cqfn.diktat.ruleset.constants.Warnings.BRACES_BLOCK_STRUCTURE_ERROR
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.rules.files.FileSize
import org.cqfn.diktat.ruleset.utils.findChildBefore
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.cqfn.diktat.ruleset.utils.prettyPrint
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType
import org.jetbrains.kotlin.psi.KtIfExpression
import org.slf4j.LoggerFactory

class BlockStructureBraces : Rule("block-structure"){
    companion object {
        private val log = LoggerFactory.getLogger(FileSize::class.java)
    }

    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var fileName: String? = null
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.getDiktatConfigRules()
        fileName = params.fileName
        emitWarn = emit
        isFixMode = autoCorrect

        when(node.elementType) {
            FUN -> checkFun(node)
            IF -> checkIf(node)
            WHEN -> checkWhen(node)
            FOR -> checkFor(node)
        }
    }

    private fun checkFor(node: ASTNode) {
        checkOpenBrace(node, BODY)
        checkCloseBrace(node.findChildByType(BODY)!!.findChildByType(BLOCK)!!)
    }

    private fun checkWhen(node: ASTNode) {
        checkOpenBrace(node, LBRACE)
        checkCloseBrace(node)
    }

    private fun checkFun(node: ASTNode) {
        if (node.hasChildOfType(BLOCK)) {
            checkOpenBrace(node, BLOCK)
            checkCloseBrace(node.findChildByType(BLOCK)!!)
        }
    }

    private fun checkIf(node: ASTNode) {
        val ifPsi = node.psi as KtIfExpression
        val thenNode = ifPsi.then?.node
        val hasElseBranch = ifPsi.elseKeyword != null
        val elseNode = ifPsi.`else`?.node

        checkOpenBrace(node, THEN)
        checkCloseBrace(thenNode!!)
        if (hasElseBranch && elseNode!!.elementType != IF){
            checkOpenBrace(node, ELSE)
            checkCloseBrace(elseNode)
        }
    }

    private fun checkOpenBrace(node: ASTNode, beforeType: IElementType) {
        val braceSpace = node.findChildBefore(beforeType, WHITE_SPACE)
        if (!(braceSpace == null || braceSpace.elementType != WHITE_SPACE)){
            if (braceSpace.text.contains("\n".toRegex())) {
                BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "open braces", 0) {
                }
            }
        }
        val nodeText = when(node.elementType){
            IF -> {
                when(beforeType) {
                    THEN -> node.findChildByType(THEN)?.text
                    else -> node.findChildByType(ELSE)?.text
                }
            }
            WHEN -> node.findChildBefore(WHEN_ENTRY, WHITE_SPACE)?.text + node.getFirstChildWithType(WHEN_ENTRY)?.text
            FOR -> node.findChildByType(BODY)?.text
            else -> node.findChildByType(BLOCK)?.text
        }
        if (nodeText != null && nodeText[1] != '\n') {
            BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "open braces", 0) {
            }
        }
    }

    private fun checkCloseBrace(node: ASTNode){
        val space = node.getChildren(null).findLast { it.elementType == WHITE_SPACE }
        if (!space!!.text.contains("\n".toRegex())) {
            BRACES_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "close braces", 0) {
            }
        }
    }
}