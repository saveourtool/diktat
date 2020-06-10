package com.huawei.rri.fixbot.ruleset.huawei.rules

import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings
import com.huawei.rri.fixbot.ruleset.huawei.utils.getAllChildrenWithType
import com.huawei.rri.fixbot.ruleset.huawei.utils.getFirstChildWithType
import com.huawei.rri.fixbot.ruleset.huawei.utils.getIdentifierName
import com.huawei.rri.fixbot.ruleset.huawei.utils.hasChildOfType
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.INTERNAL_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PROTECTED_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PUBLIC_KEYWORD
import config.rules.isRuleEnabled
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Rules for comments from chapter 2. Mainly focused on KDocs
 * 1) All top-level (file level) functions and classes with public or internal access shoudl have KDoc
 */
class KdocComments : Rule("kdoc-comments") {
    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        params: KtLint.Params,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        if (node.elementType == FILE) {
            // checking that all top level class declarations and functions have kDoc
            node.getAllChildrenWithType(CLASS).forEach { checkDocumentation(it, params, emit) }
            node.getAllChildrenWithType(FUN).forEach { checkDocumentation(it, params, emit) }
        }
    }

    private fun checkDocumentation(
        node: ASTNode,
        params: KtLint.Params,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        val kDoc = node.getFirstChildWithType(KDOC)
        val modifier = node.getFirstChildWithType(MODIFIER_LIST)
        val name = node.getIdentifierName()

        if (params.rulesConfigList!!.isRuleEnabled(Warnings.MISSING_KDOC_TOP_LEVEL) && isAccessibleOutside(modifier) && kDoc == null) {
            emit(node.startOffset,
                "${Warnings.MISSING_KDOC_TOP_LEVEL.warnText} ${name!!.text}",
                false
            )
        }
    }

    private fun isAccessibleOutside(modifierList: ASTNode?): Boolean {
        return modifierList == null ||
            modifierList.hasChildOfType(PUBLIC_KEYWORD) ||
            modifierList.hasChildOfType(INTERNAL_KEYWORD) ||
            // default == public modifier
            (!modifierList.hasChildOfType(PUBLIC_KEYWORD) && !modifierList.hasChildOfType(INTERNAL_KEYWORD) &&
                !modifierList.hasChildOfType(PROTECTED_KEYWORD) && !modifierList.hasChildOfType(PRIVATE_KEYWORD))
    }
}
