@file:Suppress(
    "MISSING_KDOC_TOP_LEVEL",
    "KDOC_NO_CONSTRUCTOR_PROPERTY",
    "MISSING_KDOC_CLASS_ELEMENTS",
    "MISSING_KDOC_ON_FUNCTION",
    "KDOC_WITHOUT_PARAM_TAG",
    "KDOC_WITHOUT_RETURN_TAG",
    "KDOC_NO_EMPTY_TAGS"
)

package com.saveourtool.diktat.ruleset.utils.search

import com.saveourtool.diktat.ruleset.utils.findAllDescendantsWithSpecificType
import com.saveourtool.diktat.ruleset.utils.isGoingAfter

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty

class VariablesWithUsagesSearch(fileNode: ASTNode,
                                filterForVariables: (KtProperty) -> Boolean) : VariablesSearch(fileNode, filterForVariables) {
    override fun KtElement.getAllSearchResults(property: KtProperty) = this.node
        .findAllDescendantsWithSpecificType(KtNodeTypes.REFERENCE_EXPRESSION)
        // filtering out all usages that are declared in the same context but are going before the variable declaration
        .filter { it.isGoingAfter(property.node) }
        .map { it.psi as KtNameReferenceExpression }
        .filter { it.getReferencedNameAsName() == property.nameAsName }
        .filterNot { expression ->
            // to avoid false triggering on objects' fields with same name as property
            expression.isReferenceToFieldOfObject() ||
                    // to exclude usages of local properties from other context (shadowed) and lambda arguments with same name
                    isReferenceToOtherVariableWithSameName(expression, this, property)
        }
}

/**
 * the default value for filtering condition is always true
 */
fun ASTNode.findAllVariablesWithUsages(filterForVariables: (KtProperty) -> Boolean = ::default) =
    VariablesWithUsagesSearch(this, filterForVariables).collectVariables()
