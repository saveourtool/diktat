package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.COLON
import com.pinterest.ktlint.core.ast.ElementType.COMMA
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.cqfn.diktat.ruleset.constants.Warnings.TOO_MANY_SPACES
import org.cqfn.diktat.ruleset.constants.Warnings.NO_SPACE_COLON
import org.cqfn.diktat.ruleset.constants.Warnings.NO_SPACE_COMMA
import org.cqfn.diktat.ruleset.utils.*

class NoSpacesRule : Rule("no-spaces") {
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
            PROPERTY -> checkProperty(node)
//            FUN -> checkFunction(node)
//            CLASS -> checkClass(node)
        }

        //print(node.prettyPrint())
    }




    private fun checkProperty(node: ASTNode){
        checkSpaces(node)

        if (node.hasChildMatching { it.elementType == COLON }) {
            node.findChildAfter(COLON, WHITE_SPACE) ?: NO_SPACE_COLON
                    .warn(configRules, emitWarn, isFixMode, "no space after colon", node.startOffset)
        }

        //print(node.prettyPrint())
    }

    private fun checkClass(node: ASTNode) {
        //print(node.prettyPrint(maxLevel = 1))

        val list = node.getAllChildrenWithType(WHITE_SPACE)

        if (node.findChildBefore(WHITE_SPACE, CLASS_KEYWORD) != null) {
            list.forEach {
                if (it.text.length > 1) {
                    TOO_MANY_SPACES.warn(configRules, emitWarn, isFixMode, "${it.text.length}", node.startOffset)
                }
            }
        }
        else {
            list.drop(1).forEach {
                if (it.text.length > 1) {
                    TOO_MANY_SPACES.warn(configRules, emitWarn, isFixMode, "${it.text.length}", node.startOffset)
                }
            }
        }
    }


    private fun checkSpaces(node: ASTNode) {
        node.getChildren(TokenSet.WHITE_SPACE).forEach {
            val spaces = it.text.length
            if(spaces > 1){
                TOO_MANY_SPACES.warn(configRules, emitWarn, isFixMode, "$spaces", node.startOffset)
            }
        }
    }

    private fun checkFunction(node: ASTNode) {
        node.getAllChildrenWithType(VALUE_PARAMETER_LIST).forEach { checkParams(it) }

        node.getAllChildrenWithType(COLON).forEach {
            if (it.treeNext.elementType != WHITE_SPACE) {
                NO_SPACE_COLON.warn(configRules, emitWarn, isFixMode, "no space after colon", node.startOffset)
            }
            if (it.treePrev.elementType != WHITE_SPACE) {
                NO_SPACE_COLON.warn(configRules, emitWarn, isFixMode, "no space before colon", node.startOffset)
            }
        }

    }

    private fun checkParams(node: ASTNode) {
        node.getAllChildrenWithType(ElementType.VALUE_PARAMETER).forEach {
            it.findChildAfter(COLON, WHITE_SPACE) ?: NO_SPACE_COLON
                    .warn(configRules, emitWarn, isFixMode, "no space after colon", node.startOffset)

            checkSpaces(it)
        }

        //print(node.prettyPrint())

        node.getAllChildrenWithType(COMMA).forEach {

            if(it.treeNext.elementType != WHITE_SPACE) {
                NO_SPACE_COMMA
                        .warn(configRules, emitWarn, isFixMode, "no space after comma", node.startOffset)
            }
        }
    }
}