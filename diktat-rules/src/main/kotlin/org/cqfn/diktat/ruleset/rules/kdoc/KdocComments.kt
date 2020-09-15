package org.cqfn.diktat.ruleset.rules.kdoc

import com.pinterest.ktlint.core.KtLint.FILE_PATH_USER_DATA_KEY
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getCommonConfiguration
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_CLASS_ELEMENTS
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_TOP_LEVEL
import org.cqfn.diktat.ruleset.utils.*
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet

/**
 * This rule checks the following features in KDocs:
 * 1) All top-level (file level) functions and classes with public or internal access should have KDoc
 * 2) All internal elements in class like class, property or function should be documented with KDoc
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
        val fileName = node.getRootNode().getUserData(FILE_PATH_USER_DATA_KEY)!!
        if (!(node.hasTestAnnotation() || isLocatedInTest(fileName.splitPathToDirs(), config.testAnchors)))
            when (node.elementType) {
                FILE -> checkTopLevelDoc(node)
                CLASS -> checkClassElements(node)
            }
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
    private fun checkDoc(node: ASTNode, warning: Warnings) {
        val kdoc = node.getFirstChildWithType(KDOC)
        val modifier = node.getFirstChildWithType(MODIFIER_LIST)
        val name = node.getIdentifierName()

        if (modifier.isAccessibleOutside() && kdoc == null) {
            warning.warn(configRules, emitWarn, isFixMode, name!!.text, node.startOffset)
        }
    }
}
