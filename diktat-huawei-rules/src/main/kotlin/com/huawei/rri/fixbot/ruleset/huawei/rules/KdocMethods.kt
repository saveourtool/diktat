package com.huawei.rri.fixbot.ruleset.huawei.rules

import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings
import com.huawei.rri.fixbot.ruleset.huawei.utils.*
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.FUN_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.THROW_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import config.rules.RulesConfig
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag

/**
 * This rule checks that whenever the method has arguments, return value, can throw exceptions,
 * KDoc block should contain `@param`, `@return`, `@throws`.
 * For `@return` check methods with explicit return type are supported and methods with inferred return
 * type are supported the following way: they should either declare return type `Unit` or have `@return` tag.
 * Currently only `throw` keyword from this methods body is supported for `@throws` check.
 */
class KdocMethods : Rule("kdoc-methods") {
    // expression body of function can have a lot of 'ElementType's, this list might be not full
    private val expressionBodyTypes = setOf(ElementType.BINARY_EXPRESSION, ElementType.CALL_EXPRESSION,
        ElementType.LAMBDA_EXPRESSION, ElementType.REFERENCE_EXPRESSION, ElementType.CALLABLE_REFERENCE_EXPRESSION,
        ElementType.SAFE_ACCESS_EXPRESSION, ElementType.WHEN_CONDITION_WITH_EXPRESSION,
        ElementType.COLLECTION_LITERAL_EXPRESSION)

    private lateinit var confiRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {

        confiRules = params.rulesConfigList!!
        isFixMode = autoCorrect
        emitWarn = emit

        if (node.elementType == FUN && node.getFirstChildWithType(MODIFIER_LIST).isAccessibleOutside()) {
            checkSignatureDescription(node)
        }
    }

    private fun checkSignatureDescription(node: ASTNode) {
        val kDoc = node.getFirstChildWithType(KDOC)
        val kDocTags = kDoc?.kDocTags()

        val missingParameters = getMissingParameters(node, kDocTags)

        val paramCheckFailed = missingParameters.isNotEmpty()
        val returnCheckFailed = checkReturnCheckFailed(node, kDocTags)
        val throwsCheckFailed = checkThrowsCheckFailed(node, kDocTags)

        if (paramCheckFailed) {
            Warnings.KDOC_WITHOUT_PARAM_TAG.warnAndFix(confiRules, emitWarn, isFixMode, missingParameters.joinToString(), node.startOffset) {
                // FixMe: add separate fix here if any parameter is missing
            }
        }
        if (returnCheckFailed) {
            Warnings.KDOC_WITHOUT_RETURN_TAG.warnAndFix(confiRules, emitWarn, isFixMode, "", node.startOffset) {
                // FixMe: add separate fix here if any return tag is missing
            }
        }
        if (throwsCheckFailed) {
            Warnings.KDOC_WITHOUT_THROWS_TAG.warnAndFix(confiRules, emitWarn, isFixMode, "", node.startOffset) {
                // FixMe: add separate fix here if throws tag is missing
            }
        }

        if (kDoc == null && isFixMode) {
            val kDocTemplate = "/**\n" +
                missingParameters.joinToString("") { " * @param $it\n" } +
                (if (returnCheckFailed) " * @return\n" else "") +
                (if (throwsCheckFailed) " * @throws\n" else "") +
                " */\n"

            // we must ensure that KDoc is inserted before `fun` keyword
            val methodNode = node.getFirstChildWithType(FUN_KEYWORD)
            node.addChild(LeafPsiElement(KDOC, kDocTemplate), methodNode)
        }
    }

    private fun getMissingParameters(node: ASTNode, kDocTags: Collection<KDocTag>?): Collection<String?> {
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

    private fun checkReturnCheckFailed(node: ASTNode, kDocTags: Collection<KDocTag>?): Boolean {
        val explicitReturnType = node.getFirstChildWithType(TYPE_REFERENCE)
        val hasExplicitNotUnitReturnType = explicitReturnType != null && explicitReturnType.text != "Unit"
        val hasExplicitUnitReturnType = explicitReturnType != null && explicitReturnType.text == "Unit"
        val isFunWithExpressionBody = expressionBodyTypes.any { node.hasChildOfType(it) }
        val hasReturnKDoc = kDocTags != null && kDocTags.hasKnownKDocTag(KDocKnownTag.RETURN)
        return (hasExplicitNotUnitReturnType || isFunWithExpressionBody && !hasExplicitUnitReturnType)
            && !hasReturnKDoc
    }

    private fun checkThrowsCheckFailed(node: ASTNode, kDocTags: Collection<KDocTag>?): Boolean {
        val codeBlock = node.getFirstChildWithType(BLOCK)
        val hasThrowInMethodBody = codeBlock != null && codeBlock.findLeafWithSpecificType(THROW_KEYWORD) != null
        val hasThrowsInKdoc = kDocTags != null && kDocTags.hasKnownKDocTag(KDocKnownTag.THROWS)
        return hasThrowInMethodBody && !hasThrowsInKdoc
    }
}
