package com.huawei.rri.fixbot.ruleset.huawei.rules

import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings
import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.MISSING_KDOC_TOP_LEVEL
import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.MISSING_KDOC_CLASS_ELEMENTS
import com.huawei.rri.fixbot.ruleset.huawei.utils.*
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.INTERNAL_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.PROTECTED_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PUBLIC_KEYWORD
import config.rules.isRuleEnabled
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

/**
 * Rules for comments from chapter 2. Mainly focused on KDocs
 * 1) All top-level (file level) functions and classes with public or internal access should have KDoc
 * 2) All internal elements in class like class, property or function should be documented with KDoc
 */
class KdocComments : Rule("kdoc-comments") {
    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        params: KtLint.Params,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        when (node.elementType) {
            FILE -> checkTopLevelDoc(node, params, emit)
            CLASS -> checkClassElements(node, params, emit)
        }
    }

    private fun checkClassElements(node: ASTNode,
                                   params: KtLint.Params,
                                   emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        val modifier = node.getFirstChildWithType(MODIFIER_LIST)
        val classBody = node.getFirstChildWithType(CLASS_BODY)

        // if parent class is public or internal than we can check it's internal code elements
        if (classBody != null && isAccessibleOutside(modifier)) {
            (classBody.getAllChildrenWithType(CLASS) + classBody.getAllChildrenWithType(FUN) + classBody.getAllChildrenWithType(PROPERTY))
                .forEach { checkDoc(it, params, MISSING_KDOC_CLASS_ELEMENTS, emit) }
        }
    }

    private fun checkTopLevelDoc(node: ASTNode,
                                 params: KtLint.Params,
                                 emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) =
        // checking that all top level class declarations and functions have kDoc
        (node.getAllChildrenWithType(CLASS) + node.getAllChildrenWithType(FUN))
            .forEach { checkDoc(it, params, MISSING_KDOC_TOP_LEVEL, emit) }


    /**
     * raises warning if protected, public or internal code element does not have a Kdoc
     */
    private fun checkDoc(
        node: ASTNode,
        params: KtLint.Params,
        warning: Warnings,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        val kDoc = node.getFirstChildWithType(KDOC)
        val modifier = node.getFirstChildWithType(MODIFIER_LIST)
        val name = node.getIdentifierName()

        if (params.rulesConfigList!!.isRuleEnabled(warning) && isAccessibleOutside(modifier) && kDoc == null) {
            emit(node.startOffset,
                "${warning.warnText} ${name!!.text}",
                false
            )
        }
    }

    private fun isAccessibleOutside(modifierList: ASTNode?): Boolean {
        return modifierList == null ||
            modifierList.hasChildOfType(PUBLIC_KEYWORD) ||
            modifierList.hasChildOfType(INTERNAL_KEYWORD) ||
            modifierList.hasChildOfType(PROTECTED_KEYWORD) ||
            // default == public modifier
            (!modifierList.hasChildOfType(PUBLIC_KEYWORD) && !modifierList.hasChildOfType(INTERNAL_KEYWORD) &&
                !modifierList.hasChildOfType(PROTECTED_KEYWORD) && !modifierList.hasChildOfType(PRIVATE_KEYWORD))
    }
}
