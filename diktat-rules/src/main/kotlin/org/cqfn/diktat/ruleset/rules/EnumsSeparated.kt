package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.COMMA
import com.pinterest.ktlint.core.ast.ElementType.ENUM_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.ENUM_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.SEMICOLON
import com.pinterest.ktlint.core.ast.isWhiteSpaceWithNewline
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.constants.Warnings.ENUMS_SEPARATED
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.cqfn.diktat.ruleset.utils.prettyPrint
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

class EnumsSeparated : Rule("enum-separated") {

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

        //println(node.prettyPrint())
        if (node.elementType == CLASS)
            if (isEnum(node))
                checkEnumEntry(node)
    }

    private fun isEnum(node: ASTNode) = node.findChildByType(MODIFIER_LIST)?.hasChildOfType(ENUM_KEYWORD) ?: false

    private fun checkEnumEntry(node: ASTNode) {
        val enums = node.findChildByType(CLASS_BODY)!!.getAllChildrenWithType(ENUM_ENTRY)
        enums.forEach {
            if (!it.treeNext.isWhiteSpaceWithNewline())
                ENUMS_SEPARATED.warnAndFix(configRules, emitWarn, isFixMode, "enum constance must end with a line break",
                        node.startOffset + node.text.length) {}
        }
        if (enums.isNotEmpty())
            checkLastEnum(enums.last())
    }

    private fun checkLastEnum(node: ASTNode){
        if (!node.hasChildOfType(COMMA)){
            ENUMS_SEPARATED.warnAndFix(configRules, emitWarn, isFixMode, "last enum constance must end with a comma",
                    node.lastChildNode.startOffset) {}
        }
        if (!node.hasChildOfType(SEMICOLON)){
            ENUMS_SEPARATED.warnAndFix(configRules, emitWarn, isFixMode, "enums must end with semicolon",
                    node.lastChildNode.startOffset) {}
        } else if (!node.findChildByType(SEMICOLON)!!.treePrev.isWhiteSpaceWithNewline()){
            ENUMS_SEPARATED.warnAndFix(configRules, emitWarn, isFixMode, "semicolon must be on a new line",
                    node.lastChildNode.startOffset) {}
        }
    }
}