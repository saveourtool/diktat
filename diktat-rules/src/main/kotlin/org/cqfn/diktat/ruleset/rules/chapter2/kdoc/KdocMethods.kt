package org.cqfn.diktat.ruleset.rules.chapter2.kdoc

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getCommonConfiguration
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_TRIVIAL_KDOC_ON_FUNCTION
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_PARAM_TAG
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_RETURN_TAG
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_THROWS_TAG
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_ON_FUNCTION
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.ruleset.utils.appendNewlineMergingWhiteSpace
import org.cqfn.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import org.cqfn.diktat.ruleset.utils.findChildAfter
import org.cqfn.diktat.ruleset.utils.getBodyLines
import org.cqfn.diktat.ruleset.utils.getFilePath
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.getIdentifierName
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.cqfn.diktat.ruleset.utils.hasKnownKdocTag
import org.cqfn.diktat.ruleset.utils.hasTestAnnotation
import org.cqfn.diktat.ruleset.utils.insertTagBefore
import org.cqfn.diktat.ruleset.utils.isAccessibleOutside
import org.cqfn.diktat.ruleset.utils.isGetterOrSetter
import org.cqfn.diktat.ruleset.utils.isLocatedInTest
import org.cqfn.diktat.ruleset.utils.isOverridden
import org.cqfn.diktat.ruleset.utils.isStandardMethod
import org.cqfn.diktat.ruleset.utils.kDocTags
import org.cqfn.diktat.ruleset.utils.parameterNames
import org.cqfn.diktat.ruleset.utils.splitPathToDirs

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.ACTUAL_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CATCH
import com.pinterest.ktlint.core.ast.ElementType.COLON
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.EQ
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.KDOC_SECTION
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TAG_NAME
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TEXT
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.THIS_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.THROW
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCatchClause
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtThrowExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

/**
 * This rule checks that whenever the method has arguments, return value, can throw exceptions,
 * KDoc block should contain `@param`, `@return`, `@throws`.
 * For `@return` check methods with explicit return type are supported and methods with inferred return
 * type are supported the following way: they should either declare return type `Unit` or have `@return` tag.
 * Currently only `throw` keyword from this methods body is supported for `@throws` check.
 */
@Suppress("ForbiddenComment")
class KdocMethods(configRules: List<RulesConfig>) : DiktatRule(
    "aad-kdoc-methods",
    configRules,
    listOf(KDOC_TRIVIAL_KDOC_ON_FUNCTION, KDOC_WITHOUT_PARAM_TAG, KDOC_WITHOUT_RETURN_TAG,
        KDOC_WITHOUT_THROWS_TAG, MISSING_KDOC_ON_FUNCTION),
    setOf(VisitorModifier.RunAfterRule("$DIKTAT_RULE_SET_ID:kdoc-comments"))
) {
    /**
     * @param node
     */
    override fun logic(node: ASTNode) {
        val isModifierAccessibleOutsideOrActual: Boolean by lazy {
            node.getFirstChildWithType(MODIFIER_LIST).run {
                isAccessibleOutside() && this?.hasChildOfType(ACTUAL_KEYWORD) != true
            }
        }
        if (node.elementType == FUN && isModifierAccessibleOutsideOrActual && !node.isOverridden()) {
            val config = configRules.getCommonConfiguration()
            val filePath = node.getFilePath()
            val isTestMethod = node.hasTestAnnotation() || isLocatedInTest(filePath.splitPathToDirs(), config.testAnchors)
            if (!isTestMethod && !node.isStandardMethod() && !node.isSingleLineGetterOrSetter()) {
                checkSignatureDescription(node)
            }
        } else if (node.elementType == KDOC_SECTION) {
            checkKdocBody(node)
        }
    }

    @Suppress("UnsafeCallOnNullableType", "AVOID_NULL_CHECKS")
    private fun checkSignatureDescription(node: ASTNode) {
        val kdoc = node.getFirstChildWithType(KDOC)
        val kdocTags = kdoc?.kDocTags()
        val name = node.getIdentifierName()!!.text

        val (missingParameters, kDocMissingParameters) = getMissingParameters(node, kdocTags)

        val explicitlyThrownExceptions = getExplicitlyThrownExceptions(node) + getRethrownExceptions(node)
        val missingExceptions = explicitlyThrownExceptions
            .minus(kdocTags
                ?.filter { it.knownTag == KDocKnownTag.THROWS }
                ?.mapNotNull { it.getSubjectName() }
                ?.toSet() ?: emptySet(),
            )

        val paramCheckFailed = (missingParameters.isNotEmpty() && !node.isSingleLineGetterOrSetter()) || kDocMissingParameters.isNotEmpty()
        val returnCheckFailed = hasReturnCheckFailed(node, kdocTags)
        val throwsCheckFailed = missingExceptions.isNotEmpty()

        val anyTagFailed = paramCheckFailed || returnCheckFailed || throwsCheckFailed
        // if no tag failed, we have too little information to suggest KDoc - it would just be empty
        if (kdoc == null && anyTagFailed) {
            addKdocTemplate(node, name, missingParameters, explicitlyThrownExceptions, returnCheckFailed)
        } else if (kdoc == null && !isReferenceExpressionWithSameName(node, kdocTags)) {
            MISSING_KDOC_ON_FUNCTION.warn(configRules, emitWarn, false, name, node.startOffset, node)
        } else {
            if (paramCheckFailed) {
                handleParamCheck(node, kdoc, missingParameters, kDocMissingParameters, kdocTags)
            }
            if (returnCheckFailed) {
                handleReturnCheck(node, kdoc, kdocTags)
            }
            if (throwsCheckFailed) {
                handleThrowsCheck(node, kdoc, missingExceptions)
            }
        }
    }

    @Suppress("TYPE_ALIAS")
    private fun getMissingParameters(node: ASTNode, kdocTags: Collection<KDocTag>?): Pair<List<String?>, List<KDocTag>> {
        val parameterNames = node.parameterNames()
        val kdocParamList = kdocTags?.filter { it.knownTag == KDocKnownTag.PARAM && it.getSubjectName() != null }
        return if (parameterNames.isEmpty()) {
            Pair(emptyList(), kdocParamList ?: emptyList())
        } else if (kdocParamList != null && kdocParamList.isNotEmpty()) {
            Pair(parameterNames.minus(kdocParamList.map { it.getSubjectName() }), kdocParamList.filter { it.getSubjectName() !in parameterNames })
        } else {
            Pair(parameterNames.toList(), emptyList())
        }
    }

    private fun isReferenceExpressionWithSameName(node: ASTNode, kdocTags: Collection<KDocTag>?): Boolean {
        val lastDotQualifiedExpression = node.findChildByType(DOT_QUALIFIED_EXPRESSION)?.psi
            ?.let { (it as KtDotQualifiedExpression).selectorExpression?.text?.substringBefore('(') }
        val funName = (node.psi as KtFunction).name
        return funName == lastDotQualifiedExpression
    }

    @Suppress("WRONG_NEWLINES")
    private fun hasReturnCheckFailed(node: ASTNode, kdocTags: Collection<KDocTag>?): Boolean {
        if (node.isSingleLineGetterOrSetter()) {
            return false
        }

        val explicitReturnType = node.findChildAfter(COLON, TYPE_REFERENCE)
        val hasNotExpressionBodyTypes = allExpressionBodyTypes.any { node.hasChildOfType(it) }
        val hasExplicitNotUnitReturnType = explicitReturnType != null && explicitReturnType.text != "Unit"
        val hasExplicitUnitReturnType = explicitReturnType != null && explicitReturnType.text == "Unit"
        val isFunWithExpressionBody = node.hasChildOfType(EQ)
        val isReferenceExpressionWithSameName = node.findAllDescendantsWithSpecificType(REFERENCE_EXPRESSION).map { it.text }.contains((node.psi as KtFunction).name)
        val hasReturnKdoc = kdocTags != null && kdocTags.hasKnownKdocTag(KDocKnownTag.RETURN)
        return (hasExplicitNotUnitReturnType || isFunWithExpressionBody && !hasExplicitUnitReturnType && hasNotExpressionBodyTypes)
        && !hasReturnKdoc && !isReferenceExpressionWithSameName
    }

    private fun getExplicitlyThrownExceptions(node: ASTNode): Set<String> {
        val codeBlock = node.getFirstChildWithType(BLOCK)
        val throwKeywords = codeBlock?.findAllDescendantsWithSpecificType(THROW)
        return throwKeywords
            ?.asSequence()
            ?.map { it.psi as KtThrowExpression }
            ?.filter {
                // we only take freshly created exceptions here: `throw IAE("stuff")` vs `throw e`
                it.thrownExpression is KtCallExpression
            }
            ?.mapNotNull { it.thrownExpression?.referenceExpression()?.text }
            ?.toSet()
            ?: emptySet()
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun getRethrownExceptions(node: ASTNode) = node.findAllDescendantsWithSpecificType(CATCH).flatMap { catchClauseNode ->
        // `parameterList` is `@Nullable @IfNotParsed`
        (catchClauseNode.psi as KtCatchClause).parameterList!!.parameters
            .filter {
                // `catch (_: Exception)` - parameter can be anonymous
                it.name != "_"
            }
            .filter { param ->
                // check whether caught parameter is rethrown in the same catch clause
                (catchClauseNode.psi as KtCatchClause).catchBody?.collectDescendantsOfType<KtThrowExpression>()?.any {
                    it.thrownExpression?.referenceExpression()?.text == param.name
                } == true
            }
            .map {
                // parameter in catch statement `catch (e: Type)` should always have type
                it.typeReference!!.text
            }
    }
        .toSet()

    @Suppress("UnsafeCallOnNullableType")
    private fun handleParamCheck(node: ASTNode,
                                 kdoc: ASTNode?,
                                 missingParameters: Collection<String?>,
                                 kdocMissingParameters: List<KDocTag>,
                                 kdocTags: Collection<KDocTag>?
    ) {
        kdocMissingParameters.forEach {
            KDOC_WITHOUT_PARAM_TAG.warn(configRules, emitWarn, false,
                "${it.getSubjectName()} param isn't present in argument list", it.node.startOffset,
                it.node)
        }
        if (missingParameters.isNotEmpty()) {
            KDOC_WITHOUT_PARAM_TAG.warnAndFix(configRules, emitWarn, isFixMode,
                "${node.getIdentifierName()!!.text} (${missingParameters.joinToString()})", node.startOffset, node) {
                val beforeTag = kdocTags?.find { it.knownTag == KDocKnownTag.RETURN }
                    ?: kdocTags?.find { it.knownTag == KDocKnownTag.THROWS }
                missingParameters.forEach {
                    kdoc?.insertTagBefore(beforeTag?.node) {
                        addChild(LeafPsiElement(KDOC_TAG_NAME, "@param"))
                        addChild(PsiWhiteSpaceImpl(" "))
                        addChild(LeafPsiElement(KDOC_TEXT, it))
                    }
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleReturnCheck(node: ASTNode,
                                  kdoc: ASTNode?,
                                  kdocTags: Collection<KDocTag>?,
    ) {
        KDOC_WITHOUT_RETURN_TAG.warnAndFix(configRules, emitWarn, isFixMode, node.getIdentifierName()!!.text,
            node.startOffset, node) {
            val beforeTag = kdocTags?.find { it.knownTag == KDocKnownTag.THROWS }
            kdoc?.insertTagBefore(beforeTag?.node) {
                addChild(LeafPsiElement(KDOC_TAG_NAME, "@return"))
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleThrowsCheck(node: ASTNode,
                                  kdoc: ASTNode?,
                                  missingExceptions: Collection<String>,
    ) {
        KDOC_WITHOUT_THROWS_TAG.warnAndFix(configRules, emitWarn, isFixMode,
            "${node.getIdentifierName()!!.text} (${missingExceptions.joinToString()})", node.startOffset, node) {
            missingExceptions.forEach {
                kdoc?.insertTagBefore(null) {
                    addChild(LeafPsiElement(KDOC_TAG_NAME, "@throws"))
                    addChild(LeafPsiElement(KDOC_TEXT, " "))
                    addChild(LeafPsiElement(KDOC_TEXT, it))
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun addKdocTemplate(node: ASTNode,
                                name: String,
                                missingParameters: Collection<String?>,
                                explicitlyThrownExceptions: Collection<String>,
                                returnCheckFailed: Boolean,
    ) {
        MISSING_KDOC_ON_FUNCTION.warnAndFix(configRules, emitWarn, isFixMode, name, node.startOffset, node) {
            val kdocTemplate = "/**\n" +
                    (missingParameters.joinToString("") { " * @param $it\n" } +
                            (if (returnCheckFailed) " * @return\n" else "") +
                            explicitlyThrownExceptions.joinToString("") { " * @throws $it\n" } +
                            " */\n"
                    )
            val kdocNode = KotlinParser().createNode(kdocTemplate).findChildByType(KDOC)!!
            node.appendNewlineMergingWhiteSpace(node.firstChildNode, node.firstChildNode)
            node.addChild(kdocNode, node.firstChildNode)
        }
    }

    private fun checkKdocBody(node: ASTNode) {
        val kdocTextNodes = node.getChildren(TokenSet.create(KDOC_TEXT))
        if (kdocTextNodes.size == 1) {
            val kdocText = kdocTextNodes
                .first()
                .text
                .trim()
            if (kdocText.matches(uselessKdocRegex)) {
                KDOC_TRIVIAL_KDOC_ON_FUNCTION.warn(configRules, emitWarn, isFixMode, kdocText, kdocTextNodes.first().startOffset, node)
            }
        }
    }

    private fun ASTNode.isSingleLineGetterOrSetter(): Boolean {
        val dotQualifiedExp = this.findChildByType(DOT_QUALIFIED_EXPRESSION)?.psi?.let { it as KtDotQualifiedExpression }
        val isThisExpression = dotQualifiedExp != null && dotQualifiedExp.receiverExpression.node.elementType == THIS_EXPRESSION
        val isExpressionBodyTypes = expressionBodyTypes.any { hasChildOfType(it) }
        return isGetterOrSetter() && (isExpressionBodyTypes || getBodyLines().size == 1 || isThisExpression)
    }

    companion object {
        val nameId = "aad-kdoc-methods"
        private val expressionBodyTypes = setOf(CALL_EXPRESSION, REFERENCE_EXPRESSION)
        private val allExpressionBodyTypes = setOf(
            DOT_QUALIFIED_EXPRESSION,
            CALL_EXPRESSION,
            REFERENCE_EXPRESSION,
            ElementType.BINARY_EXPRESSION,
            ElementType.LAMBDA_EXPRESSION,
            ElementType.CALLABLE_REFERENCE_EXPRESSION,
            ElementType.SAFE_ACCESS_EXPRESSION,
            ElementType.WHEN_CONDITION_WITH_EXPRESSION,
            ElementType.COLLECTION_LITERAL_EXPRESSION,
        )
        private val uselessKdocRegex = """^([rR]eturn|[gGsS]et)[s]?\s+\w+(\s+\w+)?$""".toRegex()
    }
}
