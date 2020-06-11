package com.huawei.rri.fixbot.ruleset.huawei.rules

import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings
import com.huawei.rri.fixbot.ruleset.huawei.utils.*
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import config.rules.isRuleEnabled
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag

/**
 * This rule checks that whenever the method has arguments, return value, can throw exceptions,
 * KDoc block should contain `@param`, `@return`, `@throws`.
 * Currently only methods with explicit return type are supported for `@return` check,
 * and only throws from this methods body for `@throws` check.
 */
class KdocMethods : Rule("kdoc-methods") {
    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        if (node.elementType == ElementType.FUN
            && isAccessibleOutside(node.getFirstChildWithType(ElementType.MODIFIER_LIST))) {
            checkSignatureDescription(node, params, autoCorrect, emit)
        }
    }

    private fun checkSignatureDescription(
        node: ASTNode,
        params: KtLint.Params,
        autoCorrect: Boolean,
        emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        val kDoc = node.getFirstChildWithType(ElementType.KDOC)
        val kDocTags = node.kDocTags()

        val missingParameters = getMissingParameters(params, node, kDocTags)

        val paramCheckFailed = missingParameters.isNotEmpty()
        val returnCheckFailed = checkReturnCheckFailed(params, node, kDocTags)
        val throwsCheckFailed = checkThrowsCheckFailed(params, node, kDocTags)

        if (paramCheckFailed) {
            emit(node.startOffset,
                "${Warnings.KDOC_WITHOUT_PARAM_TAG.warnText} ${missingParameters.joinToString()}",
                kDoc == null
            )
        }
        if (returnCheckFailed) {
            emit(node.startOffset,
                Warnings.KDOC_WITHOUT_RETURN_TAG.warnText,
                kDoc == null
            )
        }
        if (throwsCheckFailed) {
            emit(node.startOffset,
                Warnings.KDOC_WITHOUT_THROWS_TAG.warnText,
                kDoc == null
            )
        }

        if (kDoc == null && autoCorrect) {
            val kDocTemplate = "/**\n" +
                missingParameters.joinToString("") { " * @param $it\n" } +
                (if (returnCheckFailed) " * @return\n" else "") +
                (if (throwsCheckFailed) " * @throws\n" else "") +
                " */\n"

            // we must ensure that KDoc is inserted before `fun` keyword
            val methodNode = node.getFirstChildWithType(ElementType.FUN_KEYWORD)
            node.addChild(LeafPsiElement(ElementType.KDOC, kDocTemplate), methodNode)
        }
    }

    private fun getMissingParameters(params: KtLint.Params, node: ASTNode, kDocTags: Collection<KDocTag>?): Collection<String?> {
        if (!params.rulesConfigList!!.isRuleEnabled(Warnings.KDOC_WITHOUT_PARAM_TAG)) {
            return listOf()
        }
        val parameterNames = node.parameterNames()
        val kDocParameterNames = kDocTags?.filter { it.knownTag == KDocKnownTag.PARAM }
            ?.map { it.getSubjectName() }
        return if (parameterNames == null || parameterNames.isEmpty()) {
            listOf()
        } else if (kDocParameterNames != null && kDocParameterNames.isNotEmpty()) {
            parameterNames.minus(kDocParameterNames)
        } else {
            parameterNames
        }
    }

    private fun checkReturnCheckFailed(params: KtLint.Params, node: ASTNode, kDocTags: Collection<KDocTag>?): Boolean {
        return if (params.rulesConfigList!!.isRuleEnabled(Warnings.KDOC_WITHOUT_RETURN_TAG)) {
            // fixme: how to get return type for function with expression body?
            val explicitReturnType = node.getFirstChildWithType(ElementType.TYPE_REFERENCE)
            val hasExplicitNotUnitReturnType = explicitReturnType != null && explicitReturnType.text != "Unit"
            val hasReturnKDoc = kDocTags != null && kDocTags.hasKnownKDocTag(KDocKnownTag.RETURN)
            hasExplicitNotUnitReturnType && !hasReturnKDoc
        } else {
            false
        }
    }

    private fun checkThrowsCheckFailed(params: KtLint.Params, node: ASTNode, kDocTags: Collection<KDocTag>?): Boolean {
        return if (params.rulesConfigList!!.isRuleEnabled(Warnings.KDOC_WITHOUT_THROWS_TAG)) {
            val codeBlock = node.getFirstChildWithType(ElementType.BLOCK)
            val hasThrowInMethodBody = codeBlock != null && codeBlock.findLeafWithSpecificType(ElementType.THROW_KEYWORD) != null
            val hasThrowsInKdoc = kDocTags != null && kDocTags.hasKnownKDocTag(KDocKnownTag.THROWS)
            hasThrowInMethodBody && !hasThrowsInKdoc
        } else {
            false
        }
    }
}
