package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

/**
 * [this] - root node of a type File that is used to search all declared properties (variables)
 * and it's usages (rvalues).
 *
 * @return a map of a property to it's usages
 */
fun ASTNode.collectAllVariablesWithUsagesInFile(): Map<KtProperty, List<KtNameReferenceExpression>> {
    val fileNode = this.psi
    require(fileNode is KtFile) {
        "To collect all variables in a file you need to provide file root node"
    }

    return this
            .findAllNodesWithSpecificType(ElementType.PROPERTY)
            .map { it.psi as KtProperty }
            .associateWith(::findUsagesOf)
            .filterNot { it.value.isEmpty() }
}

/**
 * Finds all references to [property] in the same code block.
 * @return list of references as [KtNameReferenceExpression]
 */
fun findUsagesOf(property: KtProperty): List<KtNameReferenceExpression> {
    return property
            .getDeclarationScope()
            .let { declarationScope ->
                val variableName = property.nameAsName
                declarationScope?.findAllUsagesOfVariableInBlocks(variableName) ?: emptyList()
            }
}

/**
 * getting all usages of a variable inside the same block (where variable was declared)
 */
private fun KtBlockExpression.findAllUsagesOfVariableInBlocks(propertyName: Name?): List<KtNameReferenceExpression> {
    return this
            .node
            .findAllNodesWithSpecificType(ElementType.REFERENCE_EXPRESSION)
            .map { it.psi as KtNameReferenceExpression }
            .filter { it.getReferencedNameAsName() == propertyName }
            .filterNot {
                // to avoid false triggering on objects' fields with same name as local property
                (it.parent as? KtDotQualifiedExpression)?.run {
                    receiverExpression != it && selectorExpression?.referenceExpression() == it
                } ?: false
            }
            .filterNot { ref ->
                // to exclude usages of local properties and lambda arguments with same name
                ref.parents.mapNotNull { it as? KtBlockExpression }.takeWhile { it != this }.any { block ->
                    block.getChildrenOfType<KtProperty>().any { it.nameAsName == propertyName } ||
                            (block.parent.let { it as? KtFunctionLiteral }?.valueParameters?.any { it.nameAsName == propertyName }
                                    ?: false)
                }
            }
}
