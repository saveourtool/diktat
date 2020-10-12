package org.cqfn.diktat.ruleset.utils.search

import com.pinterest.ktlint.core.ast.ElementType
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.isGoingAfter
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty

class VariablesWithUsagesSearch(fileNode: ASTNode,
                                filterForVariables: (KtProperty) -> Boolean) : VariablesSearch(fileNode, filterForVariables) {

    override fun KtElement.getAllSearchResults(property: KtProperty): List<KtNameReferenceExpression> {
        return this.node.findAllNodesWithSpecificType(ElementType.REFERENCE_EXPRESSION)
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
}

// the default value for filtering condition is always true
fun ASTNode.findAllVariablesWithUsages(filterForVariables: (KtProperty) -> Boolean = ::default) =
        VariablesWithUsagesSearch(this, filterForVariables).collectVariables()

