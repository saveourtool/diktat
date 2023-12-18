package com.saveourtool.diktat.ruleset.rules.chapter2.kdoc

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getCommonConfiguration
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings
import com.saveourtool.diktat.ruleset.constants.Warnings.COMMENTED_BY_KDOC
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_DUPLICATE_PROPERTY
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_EXTRA_PROPERTY
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_NO_CLASS_BODY_PROPERTIES_IN_HEADER
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_NO_CONSTRUCTOR_PROPERTY
import com.saveourtool.diktat.ruleset.constants.Warnings.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT
import com.saveourtool.diktat.ruleset.constants.Warnings.MISSING_KDOC_CLASS_ELEMENTS
import com.saveourtool.diktat.ruleset.constants.Warnings.MISSING_KDOC_TOP_LEVEL
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*

import org.jetbrains.kotlin.KtNodeTypes.BLOCK
import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.KtNodeTypes.CLASS_BODY
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.MODIFIER_LIST
import org.jetbrains.kotlin.KtNodeTypes.PRIMARY_CONSTRUCTOR
import org.jetbrains.kotlin.KtNodeTypes.PROPERTY
import org.jetbrains.kotlin.KtNodeTypes.TYPE_PARAMETER
import org.jetbrains.kotlin.KtNodeTypes.TYPE_PARAMETER_LIST
import org.jetbrains.kotlin.KtNodeTypes.TYPE_REFERENCE
import org.jetbrains.kotlin.KtNodeTypes.VALUE_PARAMETER
import org.jetbrains.kotlin.KtNodeTypes.VALUE_PARAMETER_LIST
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiCommentImpl
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.KDOC
import org.jetbrains.kotlin.kdoc.parser.KDocElementTypes
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.lexer.KtTokens.BLOCK_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.lexer.KtTokens.VAL_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.VAR_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

/**
 * This rule checks the following features in KDocs:
 * 1) All top-level (file level) functions and classes with public or internal access should have KDoc
 * 2) All internal elements in class like class, property or function should be documented with KDoc
 * 3) All non-private properties declared in the primary constructor are documented using `@property` tag in class KDoc
 */
class KdocComments(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(KDOC_EXTRA_PROPERTY, KDOC_NO_CONSTRUCTOR_PROPERTY,
        KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT, MISSING_KDOC_CLASS_ELEMENTS, MISSING_KDOC_TOP_LEVEL,
        KDOC_DUPLICATE_PROPERTY, COMMENTED_BY_KDOC
    )
) {
    private val configuration by lazy {
        KdocCommentsConfiguration(configRules.getRuleConfig(KDOC_NO_CONSTRUCTOR_PROPERTY)?.configuration ?: emptyMap())
    }

    /**
     * @param node
     */
    override fun logic(node: ASTNode) {
        val filePath = node.getFilePath()
        val config = configRules.getCommonConfiguration()

        if (!node.hasTestAnnotation() && !isLocatedInTest(filePath.splitPathToDirs(), config.testAnchors)) {
            when (node.elementType) {
                KtFileElementType.INSTANCE -> checkTopLevelDoc(node)
                CLASS -> checkClassElements(node)
                BLOCK -> checkKdocPresent(node)
                else -> {
                    // this is a generated else block
                }
            }
        }
    }

    private fun checkKdocPresent(node: ASTNode) {
        node.findChildrenMatching { it.elementType == KDOC }.forEach {
            warnKdocInsideBlock(it)
        }
    }

    private fun warnKdocInsideBlock(kdocNode: ASTNode) {
        COMMENTED_BY_KDOC.warnAndFix(
            configRules, emitWarn, isFixMode,
            "Redundant asterisk in block comment: \\**", kdocNode.startOffset, kdocNode
        ) {
            kdocNode.treeParent.addChild(PsiCommentImpl(BLOCK_COMMENT, kdocNode.text.replace("/**", "/*")), kdocNode)
            kdocNode.treeParent.removeChild(kdocNode)
        }
    }

    private fun checkParameterList(
        classNode: ASTNode,
        typeParameterListNode: ASTNode?,
        valueParameterListNode: ASTNode?
    ) {
        if (typeParameterListNode == null && valueParameterListNode == null) {
            return
        }

        val kdocBeforeClass = classNode.findChildByType(KDOC)
            ?: return

        val parametersInKdoc = kdocBeforeClass
            .kDocTags()
            .filter { it.knownTag == KDocKnownTag.PROPERTY || it.knownTag == KDocKnownTag.PARAM }

        val getParametersFromListNode = { parameterListNode: ASTNode? ->
            parameterListNode?.let { node ->
                val parameterType = if (parameterListNode.elementType == TYPE_PARAMETER_LIST) TYPE_PARAMETER else VALUE_PARAMETER

                node.findChildrenMatching { it.elementType == parameterType }
                    .mapNotNull { it.findChildByType(IDENTIFIER)?.text }
            } ?: emptyList()
        }

        val parametersInTypeParameterList = getParametersFromListNode(typeParameterListNode)
        val parametersInValueParameterList = getParametersFromListNode(valueParameterListNode)

        parametersInKdoc
            .filter { it.getSubjectName() != null && it.getSubjectName() !in (parametersInTypeParameterList + parametersInValueParameterList) }
            .forEach { KDOC_EXTRA_PROPERTY.warn(configRules, emitWarn, it.text, it.node.startOffset, classNode) }
    }

    @Suppress("UnsafeCallOnNullableType", "ComplexMethod")
    private fun checkValueParameter(classNode: ASTNode, valueParameterNode: ASTNode) {
        if (valueParameterNode.parents().any { it.elementType == TYPE_REFERENCE }) {
            return
        }

        val prevComment = if (valueParameterNode.siblings(forward = false)
            .takeWhile { it.elementType != EOL_COMMENT && it.elementType != BLOCK_COMMENT }
            .all { it.elementType == WHITE_SPACE }
        ) {
            // take previous comment, if it's immediately before `valueParameterNode` or separated only with white space
            valueParameterNode.prevSibling { it.elementType == EOL_COMMENT || it.elementType == BLOCK_COMMENT }
        } else if (valueParameterNode.hasChildOfType(KDOC)) {
            valueParameterNode.findChildByType(KDOC)!!
        } else {
            null
        }

        val kdocBeforeClass = classNode.findChildByType(KDOC)
        val isParamTagNeeded = (!valueParameterNode.hasChildOfType(VAL_KEYWORD) && !valueParameterNode.hasChildOfType(VAR_KEYWORD)) ||
                !valueParameterNode.getFirstChildWithType(MODIFIER_LIST).isAccessibleOutside()

        prevComment?.let {
            kdocBeforeClass?.let {
                checkKdocBeforeClass(valueParameterNode, kdocBeforeClass, prevComment, isParamTagNeeded)
            }
                ?: createKdocWithProperty(valueParameterNode, prevComment, isParamTagNeeded)
        }
            ?: kdocBeforeClass?.let {
                checkBasicKdocBeforeClass(valueParameterNode, kdocBeforeClass, isParamTagNeeded)
            }
            ?: createKdocBasicKdoc(valueParameterNode, isParamTagNeeded)
    }

    @Suppress("UnsafeCallOnNullableType", "ComplexMethod")
    private fun checkTypeParameter(classNode: ASTNode, typeParameterNode: ASTNode) {
        val kdocBeforeClass = classNode.findChildByType(KDOC)

        kdocBeforeClass?.let {
            checkBasicKdocBeforeClass(typeParameterNode, kdocBeforeClass, true)
        } ?: createKdocBasicKdoc(typeParameterNode, true)
    }

    @Suppress("UnsafeCallOnNullableType", "ComplexMethod")
    private fun checkBasicKdocBeforeClass(
        node: ASTNode,
        kdocBeforeClass: ASTNode,
        isParamTagNeeded: Boolean
    ) {
        val parameterName = getParameterName(node)
        val parameterTagInClassKdoc = findParameterTagInClassKdoc(kdocBeforeClass, parameterName)

        parameterTagInClassKdoc?.let {
            val correctTag = if (isParamTagNeeded) KDocKnownTag.PARAM else KDocKnownTag.PROPERTY

            if (parameterTagInClassKdoc.knownTag != correctTag) {
                val warningText = if (isParamTagNeeded) {
                    "change `@property` tag to `@param` tag for <$parameterName> to KDoc"
                } else {
                    "change `@param` tag to `@property` tag for <$parameterName> to KDoc"
                }

                KDOC_NO_CONSTRUCTOR_PROPERTY.warnAndFix(configRules, emitWarn, isFixMode, warningText, node.startOffset, node) {
                    val isFirstTagInKdoc = parameterTagInClassKdoc.node == kdocBeforeClass.kDocTags().first().node
                    replaceWrongTagInClassKdoc(kdocBeforeClass, parameterName, isParamTagNeeded, isFirstTagInKdoc)
                }
            }
        } ?: run {
            if (isNeedToWarn(node, isParamTagNeeded)) {
                val warningText = if (isParamTagNeeded) "add param <$parameterName> to KDoc" else "add property <$parameterName> to KDoc"

                KDOC_NO_CONSTRUCTOR_PROPERTY.warnAndFix(configRules, emitWarn, isFixMode, warningText, node.startOffset, node) {
                    val newKdocText = if (isParamTagNeeded) "* @param $parameterName\n " else "* @property $parameterName\n "
                    insertTextInKdoc(kdocBeforeClass, checkOneNewLineAfterKdocClassDescription(kdocBeforeClass, newKdocText, false))
                }
            }
        }
    }

    private fun isNeedToWarn(node: ASTNode, isParamTagNeeded: Boolean): Boolean {
        val isParameter = node.elementType == VALUE_PARAMETER && !node.hasChildOfType(VAL_KEYWORD) && !node.hasChildOfType(VAR_KEYWORD)
        val isPrivateProperty = node.elementType == VALUE_PARAMETER && !node.getFirstChildWithType(MODIFIER_LIST).isAccessibleOutside()
        val isTypeParameterNode = node.elementType == TYPE_PARAMETER

        return !isParamTagNeeded ||
                (isParameter && configuration.isParamTagsForParameters) ||
                (isPrivateProperty && configuration.isParamTagsForPrivateProperties) ||
                (isTypeParameterNode && configuration.isParamTagsForGenericTypes)
    }

    private fun replaceWrongTagInClassKdoc(
        kdocBeforeClass: ASTNode,
        parameterName: String,
        isParamTagNeeded: Boolean,
        isFirstTagInKdoc: Boolean
    ) {
        val wrongTagText = if (isParamTagNeeded) "* @property $parameterName" else "* @param $parameterName"
        val replaceText = if (isParamTagNeeded) "* @param $parameterName" else "* @property $parameterName"

        changeTagInKdoc(kdocBeforeClass, wrongTagText, checkOneNewLineAfterKdocClassDescription(kdocBeforeClass, replaceText, isFirstTagInKdoc))
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun changeTagInKdoc(
        kdocBeforeClass: ASTNode,
        wrongTagText: String,
        correctTagText: String
    ) {
        val allKdocText = kdocBeforeClass.text
        val newKdocText = allKdocText.replaceFirst(wrongTagText, correctTagText)
        kdocBeforeClass.treeParent.replaceChild(kdocBeforeClass, KotlinParser().createNode(newKdocText).findChildByType(KDOC)!!)
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkOneNewLineAfterKdocClassDescription(
        kdocBeforeClass: ASTNode,
        newKdocText: String,
        isFirstTagInKdoc: Boolean
    ): String {
        val firstDescriptionSection = kdocBeforeClass
            .findChildrenMatching { it.elementType == KDocElementTypes.KDOC_SECTION }
            .firstOrNull()
        val lastTextInDescription = firstDescriptionSection
            ?.findChildrenMatching { it.elementType == KDocTokens.TEXT || it.elementType == KDocTokens.CODE_BLOCK_TEXT || it.elementType == KDocTokens.MARKDOWN_LINK }
            ?.lastOrNull { it.text.trim().isNotEmpty() }

        val isHasDescription = lastTextInDescription != null

        return newKdocText.let {text ->
            if (isHasDescription && (kdocBeforeClass.kDocTags().isEmpty() || isFirstTagInKdoc)) {
                // if we have any existing tags and current is first of them, we need to save last three nodes which are KDOC_LEADING_ASTERISK and two WHITE_SPACE around it
                // this is necessary so that first tag is on new line immediately after description
                val beforeChild = if (isFirstTagInKdoc) firstDescriptionSection!!.lastChildNode.treePrev.treePrev.treePrev else firstDescriptionSection!!.lastChildNode

                // remove all KDOC_LEADING_ASTERISK and WHITE_SPACE between last text in description and end of description
                firstDescriptionSection
                    .findChildrenMatching {
                        firstDescriptionSection.isChildAfterAnother(it, lastTextInDescription!!) &&
                                (firstDescriptionSection.isChildBeforeAnother(it, beforeChild) || it == beforeChild)
                    }
                    .forEach { firstDescriptionSection.removeChild(it) }

                "*\n $text"
            } else {
                text
            }
        }
    }

    private fun checkKdocBeforeClass(
        node: ASTNode,
        kdocBeforeClass: ASTNode,
        prevComment: ASTNode,
        isParamTagNeeded: Boolean
    ) {
        if (prevComment.elementType == KDOC || prevComment.elementType == BLOCK_COMMENT) {
            // there is a documentation before property or parameter that we can extract, and there is class KDoc, where we can move it to
            handleKdocAndBlock(node, kdocBeforeClass, prevComment, isParamTagNeeded)
        } else {
            handleCommentBefore(node, kdocBeforeClass, prevComment, isParamTagNeeded)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun createKdocBasicKdoc(node: ASTNode, isParamTagNeeded: Boolean) {
        if (isNeedToWarn(node, isParamTagNeeded)) {
            val parameterName = getParameterName(node)
            val warningText = if (isParamTagNeeded) "add param <$parameterName> to KDoc" else "add property <$parameterName> to KDoc"

            KDOC_NO_CONSTRUCTOR_PROPERTY.warnAndFix(configRules, emitWarn, isFixMode, warningText, node.startOffset, node) {
                val classNode = node.parent { it.elementType == CLASS }!!
                val newKdocText = if (isParamTagNeeded) "/**\n * @param $parameterName\n */" else "/**\n * @property $parameterName\n */"
                val newKdoc = KotlinParser().createNode(newKdocText)

                classNode.addChild(PsiWhiteSpaceImpl("\n"), classNode.firstChildNode)
                classNode.addChild(newKdoc.findChildByType(KDOC)!!, classNode.firstChildNode)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun createKdocWithProperty(
        node: ASTNode,
        prevComment: ASTNode,
        isParamTagNeeded: Boolean
    ) {
        if (isNeedToWarn(node, isParamTagNeeded)) {
            val parameterName = getParameterName(node)
            val classNode = node.parent { it.elementType == CLASS }!!

            // if property or parameter is documented with KDoc, which has a tag inside, then it can contain some additional more
            // complicated structure, that will be hard to move automatically
            val isHasTagsInConstructorKdoc = prevComment.elementType == KDOC && prevComment.kDocTags().isNotEmpty()
            val isFixable = !isHasTagsInConstructorKdoc
            val warningText = if (isParamTagNeeded) "add comment for param <$parameterName> to KDoc" else "add comment for property <$parameterName> to KDoc"

            KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnOnlyOrWarnAndFix(configRules, emitWarn, warningText, prevComment.startOffset, node, isFixable, isFixMode) {
                val paramOrPropertyTagText = if (isParamTagNeeded) "@param" else "@property"
                val newKdocText = createClassKdocTextFromComment(prevComment, parameterName, paramOrPropertyTagText)
                val newKdoc = KotlinParser().createNode(newKdocText).findChildByType(KDOC)!!

                classNode.addChild(PsiWhiteSpaceImpl("\n"), classNode.firstChildNode)
                classNode.addChild(newKdoc, classNode.firstChildNode)
                node.removeWithWhiteSpace(prevComment)
            }
        }
    }

    private fun createClassKdocTextFromComment(
        prevComment: ASTNode,
        parameterName: String,
        paramOrPropertyTagText: String
    ) = when (prevComment.elementType) {
        KDOC -> "/**\n * $paramOrPropertyTagText $parameterName${createClassKdocTextFromKdocComment(prevComment)}\n */"
        EOL_COMMENT -> "/**\n * $paramOrPropertyTagText $parameterName ${createClassKdocTextFromEolComment(prevComment)}\n */"
        else -> "/**\n * $paramOrPropertyTagText $parameterName${createClassKdocTextFromBlockComment(prevComment)}\n */"
    }

    private fun createClassKdocTextFromKdocComment(prevComment: ASTNode) =
        prevComment.text
            .removePrefix("/**")
            .removeSuffix("*/")
            .replace("\n( )*\\*( )*".toRegex(), "\n *   ")
            .trimStart(' ')
            .trimEnd(' ', '\n')
            .let { if (!it.startsWith("\n")) " $it" else it }

    private fun createClassKdocTextFromEolComment(prevComment: ASTNode) =
        prevComment.text
            .removePrefix("//")
            .trimStart(' ')
            .trimEnd(' ', '\n')

    private fun createClassKdocTextFromBlockComment(prevComment: ASTNode) =
        prevComment.text
            .removePrefix("/*")
            .removeSuffix("*/")
            .replace("\n( )*\\*( )*".toRegex(), "\n *   ")
            .trimStart(' ')
            .trimEnd(' ', '\n')
            .let { if (!it.startsWith("\n")) " $it" else it }

    @Suppress(
        "UnsafeCallOnNullableType",
        "TOO_LONG_FUNCTION",
        "ComplexMethod"
    )
    private fun handleKdocAndBlock(
        node: ASTNode,
        kdocBeforeClass: ASTNode,
        prevComment: ASTNode,
        isParamTagNeeded: Boolean
    ) {
        val parameterName = getParameterName(node)
        val parameterTagInClassKdoc = findParameterTagInClassKdoc(kdocBeforeClass, parameterName)
        val parameterInClassKdoc = parameterTagInClassKdoc?.node

        val commentText = if (prevComment.elementType == KDOC) {
            createClassKdocTextFromKdocComment(prevComment)
        } else {
            createClassKdocTextFromBlockComment(prevComment)
        }

        // if property or parameter is documented with KDoc, which has a tag inside, then it can contain some additional more
        // complicated structure, that will be hard to move automatically
        val isHasTagsInConstructorKdoc = prevComment.elementType == KDOC && prevComment.kDocTags().isNotEmpty()
        val isFixable = !isHasTagsInConstructorKdoc

        val (isHasWrongTag, warningText) = checkWrongTagAndMakeWarningText(parameterTagInClassKdoc, parameterName, isParamTagNeeded)
        val isFirstTagInKdoc = parameterTagInClassKdoc?.node != null && parameterTagInClassKdoc.node == kdocBeforeClass.kDocTags().first().node

        parameterInClassKdoc?.let {
            KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnOnlyOrWarnAndFix(configRules, emitWarn, warningText, prevComment.startOffset, node, isFixable, isFixMode) {
                // local docs should be appended to docs in class
                appendKdocTagContent(parameterInClassKdoc, commentText)

                if (isHasWrongTag) {
                    replaceWrongTagInClassKdoc(kdocBeforeClass, parameterName, isParamTagNeeded, isFirstTagInKdoc)
                }

                node.removeWithWhiteSpace(prevComment)
            }
        }
            ?: run {
                if (isNeedToWarn(node, isParamTagNeeded)) {
                    KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnOnlyOrWarnAndFix(configRules, emitWarn, warningText, prevComment.startOffset, node, isFixable, isFixMode) {
                        val newKdocText = if (isParamTagNeeded) "* @param $parameterName$commentText\n " else "* @property $parameterName$commentText\n "
                        insertTextInKdoc(kdocBeforeClass, checkOneNewLineAfterKdocClassDescription(kdocBeforeClass, newKdocText, isFirstTagInKdoc))

                        node.removeWithWhiteSpace(prevComment)
                    }
                }
            }
    }

    @Suppress(
        "UnsafeCallOnNullableType",
        "TOO_LONG_FUNCTION",
        "ComplexMethod"
    )
    private fun handleCommentBefore(
        node: ASTNode,
        kdocBeforeClass: ASTNode,
        prevComment: ASTNode,
        isParamTagNeeded: Boolean
    ) {
        val parameterName = getParameterName(node)
        val parameterTagInClassKdoc = findParameterTagInClassKdoc(kdocBeforeClass, parameterName)
        val parameterInClassKdoc = parameterTagInClassKdoc?.node

        val (isHasWrongTag, warningText) = checkWrongTagAndMakeWarningText(parameterTagInClassKdoc, parameterName, isParamTagNeeded)
        val isFirstTagInKdoc = parameterTagInClassKdoc?.node != null && parameterTagInClassKdoc.node == kdocBeforeClass.kDocTags().first().node

        parameterInClassKdoc?.let {
            KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnAndFix(configRules, emitWarn, isFixMode, warningText, prevComment.startOffset, node) {
                if (parameterInClassKdoc.hasChildOfType(KDocTokens.TEXT)) {
                    val newKdocText = parameterInClassKdoc
                        .findChildrenMatching { it.elementType == KDocTokens.TEXT || it.elementType == KDocTokens.CODE_BLOCK_TEXT }
                        .lastOrNull()
                    (newKdocText as LeafPsiElement).rawReplaceWithText("${newKdocText.text}\n *   ${createClassKdocTextFromEolComment(prevComment)}")
                } else {
                    parameterInClassKdoc.addChild(LeafPsiElement(KDocTokens.TEXT, " ${createClassKdocTextFromEolComment(prevComment)}"), null)
                }

                if (isHasWrongTag) {
                    replaceWrongTagInClassKdoc(kdocBeforeClass, parameterName, isParamTagNeeded, isFirstTagInKdoc)
                }

                node.treeParent.removeChildMergingSurroundingWhitespaces(prevComment.treePrev, prevComment, prevComment.treeNext)
            }
        }
            ?: run {
                if (isNeedToWarn(node, isParamTagNeeded)) {
                    KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnAndFix(configRules, emitWarn, isFixMode, warningText, prevComment.startOffset, node) {
                        val newKdocText = if (isParamTagNeeded) {
                            "* @param $parameterName ${createClassKdocTextFromEolComment(prevComment)}\n "
                        } else {
                            "* @property $parameterName ${createClassKdocTextFromEolComment(prevComment)}\n "
                        }

                        insertTextInKdoc(kdocBeforeClass, checkOneNewLineAfterKdocClassDescription(kdocBeforeClass, newKdocText, isFirstTagInKdoc))

                        node.treeParent.removeChildMergingSurroundingWhitespaces(prevComment.treePrev, prevComment, prevComment.treeNext)
                    }
                }
            }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun getParameterName(node: ASTNode) = node.findChildByType(IDENTIFIER)!!.text.replace("`", "")

    private fun findParameterTagInClassKdoc(kdocBeforeClass: ASTNode, parameterName: String) =
        kdocBeforeClass
            .kDocTags()
            .firstOrNull { (it.knownTag == KDocKnownTag.PARAM || it.knownTag == KDocKnownTag.PROPERTY) && it.getSubjectName() == parameterName }

    private fun checkWrongTagAndMakeWarningText(
        parameterTagInClassKdoc: KDocTag?,
        parameterName: String,
        isParamTagNeeded: Boolean
    ): Pair<Boolean, String> {
        val correctTag = if (isParamTagNeeded) KDocKnownTag.PARAM else KDocKnownTag.PROPERTY
        val isHasWrongTag = parameterTagInClassKdoc != null && parameterTagInClassKdoc.knownTag != correctTag
        val warningText = if (isHasWrongTag) {
            if (isParamTagNeeded) {
                "change `@property` tag to `@param` tag for <$parameterName> and add comment to KDoc"
            } else {
                "change `@param` tag to `@property` tag for <$parameterName> and add comment to KDoc"
            }
        } else {
            if (isParamTagNeeded) {
                "add comment for param <$parameterName> to KDoc"
            } else {
                "add comment for property <$parameterName> to KDoc"
            }
        }

        return Pair(isHasWrongTag, warningText)
    }

    private fun checkDuplicateProperties(kdoc: ASTNode) {
        val propertiesAndParams = kdoc
            .kDocTags()
            .filter { it.knownTag == KDocKnownTag.PROPERTY || it.knownTag == KDocKnownTag.PARAM }
        val traversedNodes: MutableSet<String?> = mutableSetOf()
        propertiesAndParams.forEach { parameter ->
            if (!traversedNodes.add(parameter.getSubjectName())) {
                KDOC_DUPLICATE_PROPERTY.warn(configRules, emitWarn, parameter.text, parameter.node.startOffset, kdoc)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun insertTextInKdoc(kdocBeforeClass: ASTNode, insertText: String) {
        val allKdocText = kdocBeforeClass.text
        val endKdoc = kdocBeforeClass.findChildByType(KDocTokens.END)!!.text
        val newKdocText = StringBuilder(allKdocText).insert(allKdocText.indexOf(endKdoc), insertText).toString()
        kdocBeforeClass.treeParent.replaceChild(kdocBeforeClass, KotlinParser().createNode(newKdocText).findChildByType(KDOC)!!)
    }

    /**
     * Append [content] to [kdocTagNode], e.g.
     * (`@property foo bar`, "baz") -> `@property foo bar baz`
     */
    private fun appendKdocTagContent(kdocTagNode: ASTNode, content: String) {
        kdocTagNode.findChildrenMatching { it.elementType == KDocTokens.TEXT || it.elementType == KDocTokens.CODE_BLOCK_TEXT }
            .lastOrNull()
            ?.let {
                kdocTagNode.replaceChild(
                    it,
                    LeafPsiElement(KDocTokens.TEXT, "${it.text}$content"),
                )
            } ?: kdocTagNode.addChild(LeafPsiElement(KDocTokens.TEXT, content), null)
    }

    private fun checkClassElements(classNode: ASTNode) {
        val modifierList = classNode.getFirstChildWithType(MODIFIER_LIST)
        val typeParameterList = classNode.getFirstChildWithType(TYPE_PARAMETER_LIST)
        val valueParameterList = classNode.getFirstChildWithType(PRIMARY_CONSTRUCTOR)?.getFirstChildWithType(VALUE_PARAMETER_LIST)
        val classBody = classNode.getFirstChildWithType(CLASS_BODY)
        val classKdoc = classNode.getFirstChildWithType(KDOC)

        checkParameterList(classNode, typeParameterList, valueParameterList)

        typeParameterList
            ?.findChildrenMatching { it.elementType == TYPE_PARAMETER }
            ?.forEach { checkTypeParameter(classNode, it) }

        valueParameterList
            ?.findChildrenMatching { it.elementType == VALUE_PARAMETER }
            ?.forEach { checkValueParameter(classNode, it) }

        // if parent class is public or internal than we can check it's internal code elements
        if (classBody != null && modifierList.isAccessibleOutside()) {
            classBody
                .getChildren(statementsToDocument)
                .filterNot {
                    (it.elementType == FUN && it.isStandardMethod()) || (it.elementType == FUN && it.isOverridden()) || (it.elementType == PROPERTY && it.isOverridden())
                }
                .forEach { classElement ->
                    if (classElement.elementType == PROPERTY) {
                        // we check if property declared in class body is also documented in class header via
                        // `@property` tag
                        val propertyInClassKdoc = classKdoc?.kDocTags()?.find {
                            it.knownTag == KDocKnownTag.PROPERTY && it.getSubjectName() == classElement.getIdentifierName()?.text
                        }
                        propertyInClassKdoc?.let {
                            // if property is documented as `@property`, then we suggest to move docs to the
                            // declaration inside the class body
                            KDOC_NO_CLASS_BODY_PROPERTIES_IN_HEADER.warn(configRules, emitWarn, classElement.text, classElement.startOffset, classElement)
                            return
                        }
                    }
                    // for everything else, we raise a missing kdoc warning
                    checkDoc(classElement, MISSING_KDOC_CLASS_ELEMENTS)
                }
        }
    }

    private fun checkTopLevelDoc(node: ASTNode) {
        // checking that all top level class declarations and functions have kDoc
        return (node.getAllChildrenWithType(CLASS) + node.getAllChildrenWithType(FUN))
            .forEach { checkDoc(it, MISSING_KDOC_TOP_LEVEL) }
    }

    /**
     * raises warning if protected, public or internal code element does not have a Kdoc
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun checkDoc(node: ASTNode, warning: Warnings) {
        // if there is an annotation before function, AST is a bit more complex, so we need to look for Kdoc among
        // children of modifier list
        val kdoc = node.getFirstChildWithType(KDOC) ?: node.getFirstChildWithType(MODIFIER_LIST)?.getFirstChildWithType(KDOC)
        kdoc?.let {
            checkDuplicateProperties(kdoc)
        }
        val name = node.getIdentifierName()
        val isModifierAccessibleOutsideOrActual = node.getFirstChildWithType(MODIFIER_LIST).run {
            isAccessibleOutside() && this?.hasChildOfType(KtTokens.ACTUAL_KEYWORD) != true
        }

        if (isModifierAccessibleOutsideOrActual && kdoc == null && !isTopLevelFunctionStandard(node)) {
            warning.warn(configRules, emitWarn, name!!.text, node.startOffset, node)
        }
    }

    private fun isTopLevelFunctionStandard(node: ASTNode): Boolean = node.elementType == FUN && node.isStandardMethod()

    /**
     * [RuleConfiguration] for param tags creation
     */
    private class KdocCommentsConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Create param tags for parameters without val or var
         */
        val isParamTagsForParameters = config["isParamTagsForParameters"]?.toBoolean() ?: true

        /**
         * Create param tags for private properties
         */
        val isParamTagsForPrivateProperties = config["isParamTagsForPrivateProperties"]?.toBoolean() ?: true

        /**
         * Create param tags for generic types
         */
        val isParamTagsForGenericTypes = config["isParamTagsForGenericTypes"]?.toBoolean() ?: true
    }

    companion object {
        const val NAME_ID = "kdoc-comments"
        private val statementsToDocument = TokenSet.create(CLASS, FUN, PROPERTY)
    }
}

private fun ASTNode.removeWithWhiteSpace(prevComment: ASTNode) {
    removeChildMergingSurroundingWhitespaces(
        if (prevComment.elementType == KDOC) prevComment.treeParent.treePrev else prevComment.treePrev,
        prevComment, prevComment.treeNext
    )
}

/**
 * If [child] node is surrounded by nodes with type `WHITE_SPACE`, remove [child] and merge surrounding nodes,
 * preserving only a single newline if present (i.e. leaving no empty lines after merging). In any case, [child] is removed
 * from the tree.
 */
private fun ASTNode.removeChildMergingSurroundingWhitespaces(
    prevWhiteSpaces: ASTNode,
    child: ASTNode,
    nextWhitespaces: ASTNode
) {
    if (nextWhitespaces.elementType == WHITE_SPACE && prevWhiteSpaces.elementType == WHITE_SPACE) {
        val mergedText = (prevWhiteSpaces.text + nextWhitespaces.text)
        val mergedSpace = if (mergedText.contains('\n')) {
            '\n' + mergedText.substringAfterLast('\n')
        } else {
            mergedText
        }

        child.treeParent.removeChild(prevWhiteSpaces)
        child.treeParent.replaceWhiteSpaceText(nextWhitespaces, mergedSpace)
    }
    removeChild(child)
}
