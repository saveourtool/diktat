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
fun ASTNode.collectAllDeclaredVariablesWithUsages(): Map<KtProperty, List<KtNameReferenceExpression>> {
    val fileNode = this.psi
    require(fileNode is KtFile) {
        "To collect all variables in a file you need to provide file root node"
    }

    return this
            .findAllNodesWithSpecificType(ElementType.PROPERTY)
            .map { it.psi as KtProperty }
            .associateWith { findUsagesOf(it, fileNode) }
            .filterNot { it.value.isEmpty() }
}

/**
 * Finds all references to [property] in the same code block.
 * @return list of references as [KtNameReferenceExpression]
 */
fun findUsagesOf(property: KtProperty, fileNode: KtFile): List<KtNameReferenceExpression> {
    println(fileNode.node.prettyPrint())
    return property
            .getDeclarationScope()
            .let { declarationScope ->
                val variableName = property.nameAsName
                // if declaration scope is not null - then we have found out the block where this variable is stored
                // else - it is a global variable on a file level
                declarationScope?.findAllUsagesOfVariableInBlock(variableName)
                        ?: fileNode.findAllUsagesOfVariableInFile(variableName)
            }
}

/**
 * getting all usages of a variable inside the same block (where variable was declared)
 */
private fun KtFile.findAllUsagesOfVariableInFile(propertyName: Name?): List<KtNameReferenceExpression> {
    return emptyList()
/*    return this
            .node
            .findAllNodesWithSpecificType(ElementType.REFERENCE_EXPRESSION)
            .map { it.psi as KtNameReferenceExpression }
            .filter { it.getReferencedNameAsName() == propertyName }
            .filterNot { expression ->
                // to avoid false triggering on objects' fields with same name as property
                isReferenceToFieldOfObject(expression) ||
                        // to exclude usages of local properties from other context (shadowed) and lambda arguments with same name
                        isReferenceToVariableWithSameName(expression, this, propertyName)
            }*/
}


/**
 * getting all usages of a variable inside the same block (where variable was declared)
 */
private fun KtBlockExpression.findAllUsagesOfVariableInBlock(propertyName: Name?): List<KtNameReferenceExpression> {
    return this
            .node
            .findAllNodesWithSpecificType(ElementType.REFERENCE_EXPRESSION)
            .map { it.psi as KtNameReferenceExpression }
            .filter { it.getReferencedNameAsName() == propertyName }
            .filterNot { expression ->
                // to avoid false triggering on objects' fields with same name as property
                isReferenceToFieldOfObject(expression) ||
                        // to exclude usages of local properties from other context (shadowed) and lambda arguments with same name
                        isReferenceToVariableWithSameName(expression, this, propertyName)
            }
}

/**
 * filtering object's fields (expressions) that have same name as variable
 */
private fun isReferenceToFieldOfObject(expression: KtNameReferenceExpression) =
        (expression.parent as? KtDotQualifiedExpression)?.run {
            receiverExpression != expression && selectorExpression?.referenceExpression() == expression
        } ?: false


/**
 * filtering local properties from other context (shadowed) and lambda and function arguments with same name
 */
private fun isReferenceToVariableWithSameName(expression: KtNameReferenceExpression, codeBlock: KtBlockExpression, propertyName: Name?) =
        expression.parents
                .mapNotNull { it as? KtBlockExpression }
                .takeWhile { codeBlock != it }
                .any { block ->
                    block.getChildrenOfType<KtProperty>().any { it.nameAsName == propertyName } ||
                            block.parent
                                    .let { it as? KtFunctionLiteral }
                                    ?.valueParameters
                                    ?.any { it.nameAsName == propertyName }
                            ?: false
                }
