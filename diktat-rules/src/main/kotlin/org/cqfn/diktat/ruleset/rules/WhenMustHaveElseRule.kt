package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.*
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.java.PsiBlockStatementImpl
import java.lang.StringBuilder

/**
 * Rule 3.10: 'when' statement must have else branch, unless when condition variable is enumerated or sealed type
 *
 * Current limitations and FixMe:
 * If a when statement of type enum or sealed contains all values of a enum - there is no need to have "else" branch.
 * The compiler can issue a warning when it is missing.
 */
@Suppress("ForbiddenComment")
class WhenMustHaveElseRule : Rule("no-else-in-when") {

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

        if (node.elementType == ElementType.WHEN) {
            checkEntries(node)
        }
    }


    //FixMe: If a when statement of type enum or sealed contains all values of a enum - there is no need to have "else" branch.

    private fun checkEntries(node: ASTNode) {
        if (!checkElses(node)) {
            Warnings.WHEN_WITHOUT_ELSE.warnAndFix(configRules, emitWarn, isFixMode, "else was not found", node.startOffset) {
                val whenEntryElse = CompositeElement(ElementType.WHEN_ENTRY)
                node.addChild(whenEntryElse, node.lastChildNode.treePrev)
                addChildren(whenEntryElse)
                node.addChild(PsiWhiteSpaceImpl("\n${getIndent(node)}"), whenEntryElse)
                println(node.treeParent.prettyPrint())
            }
        }
    }

    private fun checkElses(node: ASTNode): Boolean = node.findAllNodesWithSpecificType(ElementType.WHEN_ENTRY).any {
            it.hasChildOfType(ElementType.ELSE_KEYWORD)
        }

    private fun addChildren(node: ASTNode) {
        val block = PsiBlockStatementImpl()

        node.apply {
            addChild(LeafPsiElement(ElementType.ELSE_KEYWORD, "else"), null)
            addChild(PsiWhiteSpaceImpl(" "), null)
            addChild(LeafPsiElement(ElementType.ARROW, "->"), null)
            addChild(PsiWhiteSpaceImpl(" "), null)
            addChild(block, null)
        }


        block.apply{
            addChild(LeafPsiElement(ElementType.LBRACE, "{"), null)
            addChild(LeafPsiElement(ElementType.RBRACE, "}"), null)
        }

    }

    private fun getIndent(node: ASTNode) : String {
        val indent = node.findChildByType(ElementType.LBRACE)?.treeNext?.text?.length
        return if (indent != null) StringBuilder(" ").repeat(indent - 1) else ""
    }
}
