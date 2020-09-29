package org.cqfn.diktat.ruleset.rules.kdoc

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.KDOC_END
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TEXT
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.PRIMARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.core.ast.ElementType.VAL_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.VAR_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.parent
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getCommonConfiguration
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_CONSTRUCTOR_PROPERTY
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_CLASS_ELEMENTS
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_TOP_LEVEL
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * This rule checks the following features in KDocs:
 * 1) All top-level (file level) functions and classes with public or internal access should have KDoc
 * 2) All internal elements in class like class, property or function should be documented with KDoc
 * 3) All property in constructor doesn't contain comment
 */
class KdocComments(private val configRules: List<RulesConfig>) : Rule("kdoc-comments") {

    companion object {
        private val statementsToDocument = TokenSet.create(CLASS, FUN, PROPERTY)
    }

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(
            node: ASTNode,
            autoCorrect: Boolean,
            emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        emitWarn = emit
        isFixMode = autoCorrect

        val config = configRules.getCommonConfiguration().value
        val fileName = node.getRootNode().getFileName()
        if (!(node.hasTestAnnotation() || isLocatedInTest(fileName.splitPathToDirs(), config.testAnchors)))
            when (node.elementType) {
                FILE -> checkTopLevelDoc(node)
                CLASS -> checkClassElements(node)
                VALUE_PARAMETER -> checkValueParameter(node)
            }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkValueParameter(node: ASTNode) {
        if (node.parents().none { it.elementType == PRIMARY_CONSTRUCTOR } ||
                !(node.hasChildOfType(VAL_KEYWORD) || node.hasChildOfType(VAR_KEYWORD))) return
        val prevComment = if (node.treePrev.elementType == WHITE_SPACE &&
                (node.treePrev.treePrev.elementType == EOL_COMMENT ||
                        node.treePrev.treePrev.elementType == BLOCK_COMMENT)) {
            node.treePrev.treePrev
        } else if (node.hasChildOfType(KDOC)) {
            node.findChildByType(KDOC)!!
        } else if (node.treePrev.elementType == BLOCK_COMMENT || node.treePrev.elementType == EOL_COMMENT) {
            node.treePrev
        } else {
            null
        }
        val kDocBeforeClass = node.parent({ it.elementType == CLASS })!!.findChildByType(KDOC)
        if (prevComment != null) {
            if (kDocBeforeClass != null)
                checkKDocBeforeClass(node, kDocBeforeClass, prevComment)
            else
                createKDocWithProperty(node, prevComment)
        } else {
            if (kDocBeforeClass != null)
                checkBasicKDocBeforeClass(node, kDocBeforeClass)
            else
                createKDocBasicKDoc(node)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkBasicKDocBeforeClass(node: ASTNode, kDocBeforeClass: ASTNode) {
        val propertyInClassKDoc = kDocBeforeClass
                .kDocTags()
                ?.firstOrNull { it.knownTag == KDocKnownTag.PROPERTY && it.getSubjectName() == node.findChildByType(IDENTIFIER)!!.text }
        if (propertyInClassKDoc == null && node.getFirstChildWithType(MODIFIER_LIST).isAccessibleOutside()) {
            KDOC_NO_CONSTRUCTOR_PROPERTY.warnAndFix(configRules, emitWarn, isFixMode,
                    "add ${node.findChildByType(IDENTIFIER)!!.text} in KDoc", node.startOffset, node) {
                insertTextInKDoc(kDocBeforeClass, " * @property ${node.findChildByType(IDENTIFIER)!!.text}\n")
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun createKDocBasicKDoc(node: ASTNode) {
        KDOC_NO_CONSTRUCTOR_PROPERTY.warnAndFix(configRules, emitWarn, isFixMode,
                "add ${node.findChildByType(IDENTIFIER)!!.text} in KDoc", node.startOffset, node) {
            val newKDoc = KotlinParser().createNode("/**\n * @property ${node.findChildByType(IDENTIFIER)!!.text}\n */")
            val classNode = node.parent({ it.elementType == CLASS })!!
            classNode.addChild(PsiWhiteSpaceImpl("\n"), classNode.firstChildNode)
            classNode.addChild(newKDoc.findChildByType(KDOC)!!, classNode.firstChildNode)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun createKDocWithProperty(node: ASTNode, prevComment: ASTNode) {
        KDOC_NO_CONSTRUCTOR_PROPERTY.warnAndFix(configRules, emitWarn, isFixMode, prevComment.text, prevComment.startOffset, node) {
            val classNode = node.parent({ it.elementType == CLASS })!!
            val newKDocText = if (prevComment.elementType == KDOC) prevComment.text else if (prevComment.elementType == EOL_COMMENT) {
                "/**\n * @property ${node.findChildByType(IDENTIFIER)!!.text} ${prevComment.text.removePrefix("//")}\n */"
            } else {
                "/**\n * @property ${node.findChildByType(IDENTIFIER)!!.text}${prevComment.text.removePrefix("/*").removeSuffix("*/")} */"
            }
            val newKDoc = KotlinParser().createNode(newKDocText).findChildByType(KDOC)!!
            classNode.addChild(PsiWhiteSpaceImpl("\n"), classNode.firstChildNode)
            classNode.addChild(newKDoc, classNode.firstChildNode)
            if (prevComment.elementType == EOL_COMMENT) {
                node.treeParent.removeRange(prevComment, node)
            } else {
                if (prevComment.treeNext.elementType == WHITE_SPACE)
                    node.removeChild(prevComment.treeNext)
                node.removeChild(prevComment)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkKDocBeforeClass(node: ASTNode, kDocBeforeClass: ASTNode, prevComment: ASTNode) {
        val propertyInClassKDoc = kDocBeforeClass
                .kDocTags()
                ?.firstOrNull { it.knownTag == KDocKnownTag.PROPERTY && it.getSubjectName() == node.findChildByType(IDENTIFIER)!!.text }
                ?.node
        val propertyInLocalKDoc = if (prevComment.elementType == KDOC)
            prevComment
                    .kDocTags()
                    ?.firstOrNull { it.knownTag == KDocKnownTag.PROPERTY && it.getSubjectName() == node.findChildByType(IDENTIFIER)!!.text }
                    ?.node
        else
            null
        if (prevComment.elementType == KDOC || prevComment.elementType == BLOCK_COMMENT) {
            handleKDcoAndBlock(node, prevComment, kDocBeforeClass, propertyInClassKDoc, propertyInLocalKDoc)
        } else {
            KDOC_NO_CONSTRUCTOR_PROPERTY.warnAndFix(configRules, emitWarn, isFixMode, prevComment.text, prevComment.startOffset, node) {
                handleCommentBefore(node, kDocBeforeClass, prevComment, propertyInClassKDoc)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleKDcoAndBlock(node: ASTNode, prevComment: ASTNode, kDocBeforeClass: ASTNode, propertyInClassKDoc: ASTNode?, propertyInLocalKDoc: ASTNode?) {
        val KDocText = if (prevComment.elementType == KDOC)
            prevComment.text.removePrefix("/**").removeSuffix("*/")
        else
            prevComment.text.removePrefix("/*").removeSuffix("*/")
        val isFixable = (propertyInClassKDoc != null && propertyInLocalKDoc != null) ||
                (propertyInClassKDoc == null && propertyInLocalKDoc == null && KDocText.replace("\n+".toRegex(), "").lines().size != 1)
        KDOC_NO_CONSTRUCTOR_PROPERTY.warnAndFix(configRules, emitWarn, !isFixable, prevComment.text, prevComment.startOffset, node, !isFixable) {
            if (propertyInClassKDoc == null && propertyInLocalKDoc == null)
                insertTextInKDoc(kDocBeforeClass, " * @property ${node.findChildByType(IDENTIFIER)!!.text} ${KDocText.replace("\n+".toRegex(), "").removePrefix("*")}")
            else
                insertTextInKDoc(kDocBeforeClass, "${KDocText.trim()}\n")

            if (prevComment.treeNext.elementType == WHITE_SPACE)
                node.removeChild(prevComment.treeNext)
            node.removeChild(prevComment)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleCommentBefore(node: ASTNode, kDocBeforeClass: ASTNode, prevComment: ASTNode, propertyInClassKDoc: ASTNode?) {
        if (propertyInClassKDoc != null) {
            if (propertyInClassKDoc.hasChildOfType(KDOC_TEXT)) {
                val kdocText = propertyInClassKDoc.findChildByType(KDOC_TEXT)!!
                (kdocText as LeafPsiElement).replaceWithText("${kdocText.text} ${prevComment.text}")
            } else {
                propertyInClassKDoc.addChild(LeafPsiElement(KDOC_TEXT, prevComment.text), null)
            }
        } else {
            insertTextInKDoc(kDocBeforeClass, "* @property ${node.findChildByType(IDENTIFIER)!!.text} ${prevComment.text.removeRange(0, 2)}\n")
        }
        node.treeParent.removeRange(prevComment, node)
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun insertTextInKDoc(kDocBeforeClass: ASTNode, insertText: String) {
        val allKDocText = kDocBeforeClass.text
        val endKDoc = kDocBeforeClass.findChildByType(KDOC_END)!!.text
        val newKDocText = StringBuilder(allKDocText).insert(allKDocText.indexOf(endKDoc), insertText).toString()
        kDocBeforeClass.treeParent.replaceChild(kDocBeforeClass, KotlinParser().createNode(newKDocText).findChildByType(KDOC)!!)
    }

    private fun checkClassElements(node: ASTNode) {
        val modifier = node.getFirstChildWithType(MODIFIER_LIST)
        val classBody = node.getFirstChildWithType(CLASS_BODY)

        // if parent class is public or internal than we can check it's internal code elements
        if (classBody != null && modifier.isAccessibleOutside()) {
            classBody
                    .getChildren(statementsToDocument)
                    .filterNot { it.elementType == FUN && it.isStandardMethod() }
                    .forEach { checkDoc(it, MISSING_KDOC_CLASS_ELEMENTS) }
        }
    }

    private fun checkTopLevelDoc(node: ASTNode) =
            // checking that all top level class declarations and functions have kDoc
            (node.getAllChildrenWithType(CLASS) + node.getAllChildrenWithType(FUN))
                    .forEach { checkDoc(it, MISSING_KDOC_TOP_LEVEL) }


    /**
     * raises warning if protected, public or internal code element does not have a Kdoc
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun checkDoc(node: ASTNode, warning: Warnings) {
        val kdoc = node.getFirstChildWithType(KDOC)
        val modifier = node.getFirstChildWithType(MODIFIER_LIST)
        val name = node.getIdentifierName()

        if (modifier.isAccessibleOutside() && kdoc == null) {
            warning.warn(configRules, emitWarn, isFixMode, name!!.text, node.startOffset, node)
        }
    }
}
