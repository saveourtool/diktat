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
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty

class VariablesWithAssignmentSearch(fileNode: ASTNode,
                                    filterForVariables: (KtProperty) -> Boolean) : VariablesSearch(fileNode, filterForVariables) {
    /**
     * searching for all assignments of variables in current context [this]
     *
     * @param property
     * @return
     */
    override fun KtElement.getAllSearchResults(property: KtProperty) = this.node
        .findAllDescendantsWithSpecificType(KtNodeTypes.BINARY_EXPRESSION)
        // filtering out all usages that are declared in the same context but are going before the variable declaration
        // AND checking that there is an assignment
        .filter {
            // FixMe: bug is here with a search of postfix/prefix variables assignment (like ++).
            // FixMe: Currently we check only val a = 5, ++a is not checked here
            // FixMe: also there can be some tricky cases with setters, but I am not able to imagine them now
            it.isGoingAfter(property.node) &&
                    (it.psi as KtBinaryExpression).operationToken == KtTokens.EQ &&
                    (it.psi as KtBinaryExpression)
                        .left
                        ?.node
                        ?.elementType == KtNodeTypes.REFERENCE_EXPRESSION
        }
        .map { (it.psi as KtBinaryExpression).left as KtNameReferenceExpression }
        // checking that name of the property in usage matches with the name in the declaration
        .filter { it.getReferencedNameAsName() == property.nameAsName }
        .filterNot { expression ->
            // to avoid false triggering on objects' fields with same name as property
            expression.isReferenceToFieldOfObject() ||
                    // to exclude usages of local properties from other context (shadowed) and lambda arguments with same name
                    isReferenceToOtherVariableWithSameName(expression, this, property)
        }
        .toList()
}

/**
 * the default value for filtering condition is always true
 */
fun ASTNode.findAllVariablesWithAssignments(filterForVariables: (KtProperty) -> Boolean = ::default) =
    VariablesWithAssignmentSearch(this, filterForVariables).collectVariables()
