package com.saveourtool.diktat.ruleset.rules.chapter2.kdoc

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getCommonConfiguration
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_TRIVIAL_KDOC_ON_FUNCTION
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_PARAM_TAG
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_RETURN_TAG
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_THROWS_TAG
import com.saveourtool.diktat.ruleset.constants.Warnings.MISSING_KDOC_ON_FUNCTION
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.KotlinParser
import com.saveourtool.diktat.ruleset.utils.appendNewlineMergingWhiteSpace
import com.saveourtool.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import com.saveourtool.diktat.ruleset.utils.findChildAfter
import com.saveourtool.diktat.ruleset.utils.findParentNodeWithSpecificType
import com.saveourtool.diktat.ruleset.utils.getAllChildrenWithType
import com.saveourtool.diktat.ruleset.utils.getBodyLines
import com.saveourtool.diktat.ruleset.utils.getFilePath
import com.saveourtool.diktat.ruleset.utils.getFirstChildWithType
import com.saveourtool.diktat.ruleset.utils.getIdentifierName
import com.saveourtool.diktat.ruleset.utils.hasChildOfType
import com.saveourtool.diktat.ruleset.utils.hasKnownKdocTag
import com.saveourtool.diktat.ruleset.utils.hasTestAnnotation
import com.saveourtool.diktat.ruleset.utils.insertTagBefore
import com.saveourtool.diktat.ruleset.utils.isAccessibleOutside
import com.saveourtool.diktat.ruleset.utils.isAnonymousFunction
import com.saveourtool.diktat.ruleset.utils.isGetterOrSetter
import com.saveourtool.diktat.ruleset.utils.isLocatedInTest
import com.saveourtool.diktat.ruleset.utils.isOverridden
import com.saveourtool.diktat.ruleset.utils.isStandardMethod
import com.saveourtool.diktat.ruleset.utils.kDocTags
import com.saveourtool.diktat.ruleset.utils.parameterNames
import com.saveourtool.diktat.ruleset.utils.splitPathToDirs

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.KtNodeTypes.BLOCK
import org.jetbrains.kotlin.KtNodeTypes.CALL_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.CATCH
import org.jetbrains.kotlin.KtNodeTypes.DOT_QUALIFIED_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.MODIFIER_LIST
import org.jetbrains.kotlin.KtNodeTypes.REFERENCE_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.THIS_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.THROW
import org.jetbrains.kotlin.KtNodeTypes.TRY
import org.jetbrains.kotlin.KtNodeTypes.TYPE_REFERENCE
import org.jetbrains.kotlin.com.intellij.lang.ASTFactory
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.KDOC
import org.jetbrains.kotlin.kdoc.parser.KDocElementTypes
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.lexer.KtTokens.COLON
import org.jetbrains.kotlin.lexer.KtTokens.EQ
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtCatchClause
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtThrowExpression
import org.jetbrains.kotlin.psi.psiUtil.collectDescendantsOfType
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import java.lang.Class.forName

/**
 * This rule checks that whenever the method has arguments, return value, can throw exceptions,
 * KDoc block should contain `@param`, `@return`, `@throws`.
 * For `@return` check methods with explicit return type are supported and methods with inferred return
 * type are supported the following way: they should either declare return type `Unit` or have `@return` tag.
 * Currently only `throw` keyword from this methods body is supported for `@throws` check.
 */
@Suppress("ForbiddenComment")
class KdocMethods(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(KDOC_TRIVIAL_KDOC_ON_FUNCTION, KDOC_WITHOUT_PARAM_TAG, KDOC_WITHOUT_RETURN_TAG,
        KDOC_WITHOUT_THROWS_TAG, MISSING_KDOC_ON_FUNCTION)
) {
    /**
     * @param node
     */
    override fun logic(node: ASTNode) {
        val isModifierAccessibleOutsideOrActual: Boolean by lazy {
            node.getFirstChildWithType(MODIFIER_LIST).run {
                isAccessibleOutside() && this?.hasChildOfType(KtTokens.ACTUAL_KEYWORD) != true
            }
        }
        if (node.elementType == FUN && isModifierAccessibleOutsideOrActual && !node.isOverridden()) {
            val config = configRules.getCommonConfiguration()
            val filePath = node.getFilePath()
            val isTestMethod = node.hasTestAnnotation() || isLocatedInTest(filePath.splitPathToDirs(), config.testAnchors)
            if (!isTestMethod && !node.isStandardMethod() && !node.isSingleLineGetterOrSetter() && !node.isAnonymousFunction()) {
                checkSignatureDescription(node)
            }
        } else if (node.elementType == KDocElementTypes.KDOC_SECTION) {
            checkKdocBody(node)
        }
    }

    private fun hasFunParent(node: ASTNode): Boolean {
        var parent = node.treeParent
        while (parent != null) {
            if (parent.elementType == FUN) {
                return true
            }
            parent = parent.treeParent
        }
        return false
    }

    @Suppress(
        "UnsafeCallOnNullableType",
        "AVOID_NULL_CHECKS",
        "CyclomaticComplexMethod"
    )
    private fun checkSignatureDescription(node: ASTNode) {
        val kdoc = node.getFirstChildWithType(KDOC)
        val kdocTags = kdoc?.kDocTags()
        val name = node.getIdentifierName()!!.text

        val (missingParameters, kDocMissingParameters) = getMissingParameters(node, kdocTags)

        val explicitlyThrownExceptions = getExplicitlyThrownExceptions(node) + getRethrownExceptions(node)
        val missingExceptions = explicitlyThrownExceptions
            .minus((kdocTags
                ?.filter { it.knownTag == KDocKnownTag.THROWS }
                ?.mapNotNull { it.getSubjectLinkName() }
                ?.toSet() ?: emptySet()).toSet())

        val paramCheckFailed = (missingParameters.isNotEmpty() && !node.isSingleLineGetterOrSetter()) || kDocMissingParameters.isNotEmpty()
        val returnCheckFailed = hasReturnCheckFailed(node, kdocTags)
        val throwsCheckFailed = missingExceptions.isNotEmpty()

        val anyTagFailed = paramCheckFailed || returnCheckFailed || throwsCheckFailed
        // if no tag failed, we have too little information to suggest KDoc - it would just be empty
        if (kdoc == null && hasFunParent(node)) {
            return
        } else if (kdoc == null && anyTagFailed) {
            addKdocTemplate(node, name, missingParameters, explicitlyThrownExceptions, returnCheckFailed)
        } else if (kdoc == null && !isReferenceExpressionWithSameName(node)) {
            MISSING_KDOC_ON_FUNCTION.warn(configRules, emitWarn, name, node.startOffset, node)
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

    private fun KDocTag.getSubjectLinkName(): String? =
        node.getChildren(null)
            .dropWhile { it.elementType == KDocTokens.TAG_NAME }
            .dropWhile { it.elementType == WHITE_SPACE }
            .firstOrNull { it.elementType == KDocTokens.MARKDOWN_LINK }
            ?.text

    @Suppress("TYPE_ALIAS")
    private fun getMissingParameters(node: ASTNode, kdocTags: Collection<KDocTag>?): Pair<List<String?>, List<KDocTag>> {
        val parameterNames = node.parameterNames()
        val kdocParamList = kdocTags?.filter { it.knownTag == KDocKnownTag.PARAM && it.getSubjectLinkName() != null }
        return if (parameterNames.isEmpty()) {
            Pair(emptyList(), kdocParamList ?: emptyList())
        } else if (!kdocParamList.isNullOrEmpty()) {
            Pair(parameterNames.minus(kdocParamList.map { it.getSubjectLinkName() }.toSet()), kdocParamList.filter { it.getSubjectLinkName() !in parameterNames })
        } else {
            Pair(parameterNames.toList(), emptyList())
        }
    }

    private fun isReferenceExpressionWithSameName(node: ASTNode): Boolean {
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

    private fun isThrowInTryCatchBlock(node: ASTNode?): Boolean {
        node ?: return false
        val parent = node.findParentNodeWithSpecificType(TRY)
        val nodeName = node.findAllDescendantsWithSpecificType(IDENTIFIER).firstOrNull() ?: return false

        if (parent?.elementType == TRY) {
            val catchNodes = parent?.getAllChildrenWithType(CATCH)
            val findNodeWithMatchingCatch = catchNodes?.firstOrNull { catchNode ->
                val matchingNodeForCatchNode = catchNode.findAllDescendantsWithSpecificType(IDENTIFIER)
                    .firstOrNull { catchNodeName ->
                        nodeName.text == catchNodeName.text || try {
                            val nodeClass = forName("java.lang.${nodeName.text}")
                            val nodeInstance = nodeClass.getDeclaredConstructor().newInstance()
                            val catchNodeClass = forName("java.lang.${catchNodeName.text}")
                            catchNodeClass.isInstance(nodeInstance)
                        } catch (e: ClassNotFoundException) {
                            false
                        }
                    }
                matchingNodeForCatchNode != null
            }
            return findNodeWithMatchingCatch != null
        }
        return false
    }

    private fun getExplicitlyThrownExceptions(node: ASTNode): Set<String> {
        val codeBlock = node.getFirstChildWithType(BLOCK)
        val throwKeywords = codeBlock?.findAllDescendantsWithSpecificType(THROW)

        return throwKeywords
            ?.asSequence()
            ?.filter { !isThrowInTryCatchBlock(it) }
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
            KDOC_WITHOUT_PARAM_TAG.warn(configRules, emitWarn,
                "${ it.getSubjectLinkName() } param isn't present in argument list", it.node.startOffset,
                it.node)
        }
        if (missingParameters.isNotEmpty()) {
            KDOC_WITHOUT_PARAM_TAG.warnAndFix(configRules, emitWarn, isFixMode,
                "${node.getIdentifierName()!!.text} (${missingParameters.joinToString()})", node.startOffset, node) {
                val beforeTag = kdocTags?.find { it.knownTag == KDocKnownTag.RETURN }
                    ?: kdocTags?.find { it.knownTag == KDocKnownTag.THROWS }
                missingParameters.filterNotNull().forEach { missingParameter ->
                    kdoc?.insertTagBefore(beforeTag?.node) {
                        addChild(ASTFactory.leaf(KDocTokens.TAG_NAME, "@param"))
                        addChild(ASTFactory.whitespace(" "))
                        val kdocMarkdownLink = ASTFactory.composite(KDocTokens.MARKDOWN_LINK)
                            .also { addChild(it) }
                        val kdocName = ASTFactory.composite(KDocElementTypes.KDOC_NAME)
                            .also { kdocMarkdownLink.addChild(it) }
                        kdocName.addChild(ASTFactory.leaf(KtTokens.IDENTIFIER, missingParameter))
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
                addChild(LeafPsiElement(KDocTokens.TAG_NAME, "@return"))
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
                    addChild(LeafPsiElement(KDocTokens.TAG_NAME, "@throws"))
                    addChild(PsiWhiteSpaceImpl(" "))
                    addChild(LeafPsiElement(KDocTokens.MARKDOWN_LINK, it))
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
        val kdocTextNodes = node.getChildren(TokenSet.create(KDocTokens.TEXT))
        if (kdocTextNodes.size == 1) {
            val kdocText = kdocTextNodes
                .first()
                .text
                .trim()
            if (kdocText.matches(uselessKdocRegex)) {
                KDOC_TRIVIAL_KDOC_ON_FUNCTION.warn(configRules, emitWarn, kdocText, kdocTextNodes.first().startOffset, node)
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
        const val NAME_ID = "kdoc-methods"
        private val expressionBodyTypes = setOf(CALL_EXPRESSION, REFERENCE_EXPRESSION)
        private val allExpressionBodyTypes = setOf(
            DOT_QUALIFIED_EXPRESSION,
            CALL_EXPRESSION,
            REFERENCE_EXPRESSION,
            KtNodeTypes.BINARY_EXPRESSION,
            KtNodeTypes.LAMBDA_EXPRESSION,
            KtNodeTypes.CALLABLE_REFERENCE_EXPRESSION,
            KtNodeTypes.SAFE_ACCESS_EXPRESSION,
            KtNodeTypes.WHEN_CONDITION_EXPRESSION,
            KtNodeTypes.COLLECTION_LITERAL_EXPRESSION,
        )
        private val uselessKdocRegex = """^([rR]eturn|[gGsS]et)[s]?\s+\w+(\s+\w+)?$""".toRegex()
    }
}
