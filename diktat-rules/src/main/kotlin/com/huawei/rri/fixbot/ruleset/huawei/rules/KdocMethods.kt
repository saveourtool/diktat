package com.huawei.rri.fixbot.ruleset.huawei.rules

import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.KDOC_WITHOUT_PARAM_TAG
import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.KDOC_WITHOUT_RETURN_TAG
import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.KDOC_WITHOUT_THROWS_TAG
import com.huawei.rri.fixbot.ruleset.huawei.constants.Warnings.MISSING_KDOC_ON_FUNCTION
import com.huawei.rri.fixbot.ruleset.huawei.utils.findAllNodesWithSpecificType
import com.huawei.rri.fixbot.ruleset.huawei.utils.getFirstChildWithType
import com.huawei.rri.fixbot.ruleset.huawei.utils.getIdentifierName
import com.huawei.rri.fixbot.ruleset.huawei.utils.hasChildOfType
import com.huawei.rri.fixbot.ruleset.huawei.utils.hasKnownKDocTag
import com.huawei.rri.fixbot.ruleset.huawei.utils.insertTagBefore
import com.huawei.rri.fixbot.ruleset.huawei.utils.isAccessibleOutside
import com.huawei.rri.fixbot.ruleset.huawei.utils.kDocTags
import com.huawei.rri.fixbot.ruleset.huawei.utils.parameterNames
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.FUN_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TAG_NAME
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TEXT
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.THROW
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.prevSibling
import config.rules.RulesConfig
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
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

        val explicitlyThrownExceptions = getExplicitlyThrownExceptions(node)
        val missingExceptions = explicitlyThrownExceptions
            .minus(kDocTags
                ?.filter { it.knownTag == KDocKnownTag.THROWS }
                ?.map { it.getSubjectName() }
                ?.toSet() ?: setOf()
            )

        val paramCheckFailed = missingParameters.isNotEmpty()
        val returnCheckFailed = checkReturnCheckFailed(node, kDocTags)
        val throwsCheckFailed = missingExceptions.isNotEmpty()

        val name = node.getIdentifierName()!!.text
        if (paramCheckFailed) {
            KDOC_WITHOUT_PARAM_TAG.warnAndFix(confiRules, emitWarn, isFixMode,
                "$name (${missingParameters.joinToString()})", node.startOffset) {
                val beforeTag = kDocTags?.find { it.knownTag == KDocKnownTag.RETURN }
                     ?: kDocTags?.find { it.knownTag == KDocKnownTag.THROWS }
                missingParameters.forEach {
                    kDoc?.insertTagBefore(beforeTag?.node) {
                        addChild(LeafPsiElement(KDOC_TAG_NAME, "@param"))
                        addChild(PsiWhiteSpaceImpl(" "))
                        addChild(LeafPsiElement(KDOC_TEXT, it))
                    }
                }
            }
        }
        if (returnCheckFailed) {
            KDOC_WITHOUT_RETURN_TAG.warnAndFix(confiRules, emitWarn, isFixMode, name, node.startOffset) {
                val beforeTag = kDocTags?.find { it.knownTag == KDocKnownTag.THROWS }
                kDoc?.insertTagBefore(beforeTag?.node) {
                    addChild(LeafPsiElement(KDOC_TAG_NAME, "@return"))
                }
            }
        }
        if (throwsCheckFailed) {
            KDOC_WITHOUT_THROWS_TAG.warnAndFix(confiRules, emitWarn, isFixMode,
                "$name (${missingExceptions.joinToString()})", node.startOffset) {
                explicitlyThrownExceptions.forEach {
                    kDoc?.insertTagBefore(null) {
                        addChild(LeafPsiElement(KDOC_TAG_NAME, "@throws"))
                        addChild(LeafPsiElement(KDOC_TEXT, " "))
                        addChild(LeafPsiElement(KDOC_TEXT, it))
                    }
                }
            }
        }

        // if no tag failed, we have too little information to suggest KDoc - it would just be empty
        val anyTagFailed = paramCheckFailed || returnCheckFailed || throwsCheckFailed
        if (kDoc == null && anyTagFailed) {
            MISSING_KDOC_ON_FUNCTION.warnAndFix(confiRules, emitWarn, isFixMode,
                node.getIdentifierName()!!.text, node.startOffset) {
                val indent = node.prevSibling { it.elementType == WHITE_SPACE }?.text
                    ?.substringAfterLast("\n")?.count { it == ' ' } ?: 0
                val kDocTemplate = "/**\n" +
                    (missingParameters.joinToString("") { " * @param $it\n" } +
                        (if (returnCheckFailed) " * @return\n" else "") +
                        explicitlyThrownExceptions.joinToString("") { " * @throws $it\n" } +
                        " */\n"
                        ).prependIndent(" ".repeat(indent))

                // we must ensure that KDoc is inserted before `fun` keyword
                val methodNode = node.getFirstChildWithType(FUN_KEYWORD)
                // fixme could be added as proper CompositeElement
                node.addChild(LeafPsiElement(KDOC, kDocTemplate), methodNode)
            }
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

    private fun getExplicitlyThrownExceptions(node: ASTNode): Set<String> {
        val codeBlock = node.getFirstChildWithType(BLOCK)
        val throwKeywords = codeBlock?.findAllNodesWithSpecificType(THROW)
        return throwKeywords?.map {
            // fixme probably `throws` can have other expression types
            it.findChildByType(ElementType.CALL_EXPRESSION)
                ?.findChildByType(ElementType.REFERENCE_EXPRESSION)?.text!!
        }?.toSet() ?: setOf()
    }
}
