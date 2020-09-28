package org.cqfn.diktat.ruleset.rules.kdoc

import com.pinterest.ktlint.core.Rule
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
        if (node.parents().none { it.elementType == PRIMARY_CONSTRUCTOR }) return
        val prevComment = if (node.treePrev.elementType == EOL_COMMENT) node.treePrev
        else if (node.treePrev.elementType == WHITE_SPACE && node.treePrev.treePrev.elementType == EOL_COMMENT) node.treePrev.treePrev
        else if (node.hasChildOfType(KDOC)) node.findChildByType(KDOC)!!
        else return
        val kDocBeforeClass = node.parent({ it.elementType == CLASS })!!.findChildByType(KDOC)
        if (kDocBeforeClass != null)
            checkKDocBeforeClass(node, kDocBeforeClass, prevComment)
        else
            createKDocWithProperty(node, prevComment)
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun createKDocWithProperty(node: ASTNode, prevComment: ASTNode) {
        val modifier = node.getFirstChildWithType(MODIFIER_LIST)
        KDOC_NO_CONSTRUCTOR_PROPERTY.warnAndFix(configRules, emitWarn, modifier.isAccessibleOutside(),
                prevComment.text, prevComment.startOffset, node, modifier.isAccessibleOutside()) {
            val classNode = node.parent({ it.elementType == CLASS })!!
            val newKDocText = if (prevComment.elementType == KDOC) prevComment.text else {
                "/**\n * ${prevComment.text.removePrefix("//")}\n */"
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
        val isFixable = prevComment.elementType != KDOC || propertyInClassKDoc == null || propertyInLocalKDoc == null
        KDOC_NO_CONSTRUCTOR_PROPERTY.warnAndFix(configRules, emitWarn, isFixable, prevComment.text, prevComment.startOffset, node, isFixable) {
            if (prevComment.elementType == EOL_COMMENT)
                handleCommentBefore(node, kDocBeforeClass, prevComment, propertyInClassKDoc)
            else
                handleKDocBefore(node, kDocBeforeClass, prevComment)
        }
    }

    private fun handleKDocBefore(node: ASTNode, kDocBeforeClass: ASTNode, prevComment: ASTNode) {
        val insertText = "${prevComment.text.removePrefix("/**").removeSuffix("*/").trim()}\n"
        insertTextInKDoc(kDocBeforeClass, insertText)
        if (prevComment.treeNext.elementType == WHITE_SPACE)
            node.removeChild(prevComment.treeNext)
        node.removeChild(prevComment)
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleCommentBefore(node: ASTNode, kDocBeforeClass: ASTNode, prevComment: ASTNode, propertyInClassKDoc: ASTNode?) {
        if (propertyInClassKDoc != null) {
            propertyInClassKDoc.addChild(LeafPsiElement(KDOC_TEXT, prevComment.text), null)
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
