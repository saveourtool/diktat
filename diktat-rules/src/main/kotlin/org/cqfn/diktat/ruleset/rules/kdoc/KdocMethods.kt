package org.cqfn.diktat.ruleset.rules.kdoc

import com.pinterest.ktlint.core.KtLint.FILE_PATH_USER_DATA_KEY
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.CALLABLE_REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.COLLECTION_LITERAL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.KDOC_SECTION
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TAG_NAME
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TEXT
import com.pinterest.ktlint.core.ast.ElementType.LAMBDA_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.SAFE_ACCESS_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.THROW
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.WHEN_CONDITION_WITH_EXPRESSION
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_ON_FUNCTION
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_TRIVIAL_KDOC_ON_FUNCTION
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_PARAM_TAG
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_RETURN_TAG
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_THROWS_TAG
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag

/**
 * This rule checks that whenever the method has arguments, return value, can throw exceptions,
 * KDoc block should contain `@param`, `@return`, `@throws`.
 * For `@return` check methods with explicit return type are supported and methods with inferred return
 * type are supported the following way: they should either declare return type `Unit` or have `@return` tag.
 * Currently only `throw` keyword from this methods body is supported for `@throws` check.
 */
@Suppress("ForbiddenComment")
class KdocMethods(private val configRules: List<RulesConfig>) : Rule("kdoc-methods") {
    companion object {
        // expression body of function can have a lot of 'ElementType's, this list might be not full
        private val expressionBodyTypes = setOf(BINARY_EXPRESSION, CALL_EXPRESSION, LAMBDA_EXPRESSION, REFERENCE_EXPRESSION,
                CALLABLE_REFERENCE_EXPRESSION, SAFE_ACCESS_EXPRESSION, WHEN_CONDITION_WITH_EXPRESSION, COLLECTION_LITERAL_EXPRESSION)

        private val uselessKdocRegex = """^([rR]eturn|[gGsS]et)[s]?\s+\w+(\s+\w+)?$""".toRegex()
    }

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        isFixMode = autoCorrect
        emitWarn = emit

        if (node.elementType == FUN && node.getFirstChildWithType(MODIFIER_LIST).isAccessibleOutside()) {
            val config = KdocMethodsConfiguration(configRules.getRuleConfig(MISSING_KDOC_ON_FUNCTION)?.configuration
                    ?: mapOf())
            val fileName = node.getRootNode().getUserData(FILE_PATH_USER_DATA_KEY)!!
            val isTestMethod = node.hasTestAnnotation() || node.isLocatedInTest(fileName.splitPathToDirs(), config.testAnchors)
            if (!isTestMethod && !node.isStandardMethod() && !node.isSingleLineGetterOrSetter()) {
                checkSignatureDescription(node)
            }
        } else if (node.elementType == KDOC_SECTION) {
            checkKdocBody(node)
        }
    }

    private fun checkSignatureDescription(node: ASTNode) {
        val kDoc = node.getFirstChildWithType(KDOC)
        val kDocTags = kDoc?.kDocTags()
        val name = node.getIdentifierName()!!.text

        val (missingParameters, kDocMissingParameters) = getMissingParameters(node, kDocTags)

        val explicitlyThrownExceptions = getExplicitlyThrownExceptions(node)
        val missingExceptions = explicitlyThrownExceptions
                .minus(kDocTags
                        ?.filter { it.knownTag == KDocKnownTag.THROWS }
                        ?.mapNotNull { it.getSubjectName() }
                        ?.toSet() ?: setOf()
                )

        val paramCheckFailed = (missingParameters.isNotEmpty() && !node.isSingleLineGetterOrSetter()) || kDocMissingParameters.isNotEmpty()
        val returnCheckFailed = checkReturnCheckFailed(node, kDocTags)
        val throwsCheckFailed = missingExceptions.isNotEmpty()

        if (paramCheckFailed) handleParamCheck(node, kDoc, missingParameters, kDocMissingParameters, kDocTags)
        if (returnCheckFailed) handleReturnCheck(node, kDoc, kDocTags)
        if (throwsCheckFailed) handleThrowsCheck(node, kDoc, missingExceptions)

        // if no tag failed, we have too little information to suggest KDoc - it would just be empty
        val anyTagFailed = paramCheckFailed || returnCheckFailed || throwsCheckFailed
        if (kDoc == null && anyTagFailed) {
            addKdocTemplate(node, name, missingParameters, explicitlyThrownExceptions, returnCheckFailed)
        } else if (kDoc == null) {
            MISSING_KDOC_ON_FUNCTION.warn(configRules, emitWarn, false, name, node.startOffset)
        }
    }

    private fun getMissingParameters(node: ASTNode, kDocTags: Collection<KDocTag>?): Pair<List<String?>, List<KDocTag>> {
        val parameterNames = node.parameterNames()
        val kDocParamList = kDocTags?.filter { it.knownTag == KDocKnownTag.PARAM && it.getSubjectName() != null }
        return if (parameterNames == null || parameterNames.isEmpty()) {
            return Pair(listOf(), kDocParamList ?: listOf())
        } else if (kDocParamList != null && kDocParamList.isNotEmpty()) {
            Pair(parameterNames.minus(kDocParamList.map { it.getSubjectName() }), kDocParamList.filter { it.getSubjectName() !in parameterNames })
        } else {
            Pair(parameterNames.toList(), listOf())
        }
    }

    private fun checkReturnCheckFailed(node: ASTNode, kDocTags: Collection<KDocTag>?): Boolean {
        if (node.isSingleLineGetterOrSetter()) return false

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
            it.findChildByType(CALL_EXPRESSION)
                    ?.findChildByType(REFERENCE_EXPRESSION)?.text!!
        }?.toSet() ?: setOf()
    }

    private fun handleParamCheck(node: ASTNode,
                                 kDoc: ASTNode?,
                                 missingParameters: Collection<String?>,
                                 kDocMissingParameters: List<KDocTag>,
                                 kDocTags: Collection<KDocTag>?) {
        kDocMissingParameters.forEach {
            KDOC_WITHOUT_PARAM_TAG.warn(configRules, emitWarn, false,
                    "${it.getSubjectName()} param isn't define in function", it.node.startOffset)
        }
        KDOC_WITHOUT_PARAM_TAG.warnAndFix(configRules, emitWarn, isFixMode,
                "${node.getIdentifierName()!!.text} (${missingParameters.joinToString()})", node.startOffset) {
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

    private fun handleReturnCheck(node: ASTNode,
                                  kDoc: ASTNode?,
                                  kDocTags: Collection<KDocTag>?
    ) {
        KDOC_WITHOUT_RETURN_TAG.warnAndFix(configRules, emitWarn, isFixMode, node.getIdentifierName()!!.text, node.startOffset) {
            val beforeTag = kDocTags?.find { it.knownTag == KDocKnownTag.THROWS }
            kDoc?.insertTagBefore(beforeTag?.node) {
                addChild(LeafPsiElement(KDOC_TAG_NAME, "@return"))
            }
        }
    }

    private fun handleThrowsCheck(node: ASTNode,
                                  kDoc: ASTNode?,
                                  missingExceptions: Collection<String>
    ) {
        KDOC_WITHOUT_THROWS_TAG.warnAndFix(configRules, emitWarn, isFixMode,
                "${node.getIdentifierName()!!.text} (${missingExceptions.joinToString()})", node.startOffset) {
            missingExceptions.forEach {
                kDoc?.insertTagBefore(null) {
                    addChild(LeafPsiElement(KDOC_TAG_NAME, "@throws"))
                    addChild(LeafPsiElement(KDOC_TEXT, " "))
                    addChild(LeafPsiElement(KDOC_TEXT, it))
                }
            }
        }
    }

    private fun addKdocTemplate(node: ASTNode,
                                name: String,
                                missingParameters: Collection<String?>,
                                explicitlyThrownExceptions: Collection<String>,
                                returnCheckFailed: Boolean
    ) {
        MISSING_KDOC_ON_FUNCTION.warnAndFix(configRules, emitWarn, isFixMode, name, node.startOffset) {
            val kDocTemplate = "/**\n" +
                    (missingParameters.joinToString("") { " * @param $it\n" } +
                            (if (returnCheckFailed) " * @return\n" else "") +
                            explicitlyThrownExceptions.joinToString("") { " * @throws $it\n" } +
                            " */\n"
                            )
            val kdocNode = KotlinParser().createNode(kDocTemplate).findChildByType(KDOC)!!
            node.appendNewlineMergingWhiteSpace(node.firstChildNode, node.firstChildNode)
            node.addChild(kdocNode, node.firstChildNode)
        }
    }

    private fun checkKdocBody(node: ASTNode) {
        val kdocTextNodes = node.getChildren(TokenSet.create(KDOC_TEXT))
        if (kdocTextNodes.size == 1) {
            val kdocText = kdocTextNodes.first().text.trim()
            if (kdocText.matches(uselessKdocRegex)) {
                KDOC_TRIVIAL_KDOC_ON_FUNCTION.warn(configRules, emitWarn, isFixMode, kdocText, kdocTextNodes.first().startOffset)
            }
        }
    }

    private fun ASTNode.isSingleLineGetterOrSetter() = isGetterOrSetter() && (expressionBodyTypes.any { hasChildOfType(it) } || getBodyLines().size == 1)
}

private class KdocMethodsConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
    /**
     * Names of directories which indicate that this is path to tests. Will be checked like "src/$testAnchor" for each entry.
     */
    val testAnchors = config.getOrDefault("testDirs", "test").split(',')
}
