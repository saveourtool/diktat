package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClassBody
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

/**
 * [this] - root node of a type File that is used to search all declared properties (variables)
 * and it's usages (rvalues).
 * (!) ONLY for nodes of File elementType
 *
 * @return a map of a property to it's usages
 */
fun ASTNode.collectAllDeclaredVariablesWithUsages(): Map<KtProperty, List<KtNameReferenceExpression>> {
    require(this.elementType == ElementType.FILE) {
        "To collect all variables in a file you need to provide file root node"
    }

    return this
            .findAllNodesWithSpecificType(ElementType.PROPERTY)
            .map { it.psi as KtProperty }
            .associateWith { it.getAllUsages() }
}

/**
 * Finds all references to [this] in the same code block.
 * [this] - usages of this property will be searched
 * @return list of references as [KtNameReferenceExpression]
 */
@Suppress("UnsafeCallOnNullableType")
fun KtProperty.getAllUsages(): List<KtNameReferenceExpression> {
    return this
            .getDeclarationScope()
            // if declaration scope is not null - then we have found out the block where this variable is stored
            // else - it is a global variable on a file level or a property on the class level
            .let { declarationScope ->
                // searching in the scope with declaration (in the context)
                declarationScope?.getAllUsagesOfProperty(this)
                        // searching on the class level in class body
                        ?: (this.getParentOfType<KtClassBody>(true)?.getAllUsagesOfProperty(this))
                        // searching on the file level
                        ?: (this.getParentOfType<KtFile>(true)!!.getAllUsagesOfProperty(this))
            }
}

/**
 * getting all usages of a variable inside the same (or nested) block (where variable was declared)
 */
private fun KtElement.getAllUsagesOfProperty(property: KtProperty) =
        this.node.findAllNodesWithSpecificType(ElementType.REFERENCE_EXPRESSION)
                // filtering out all usages that are declared in the same context but are going before the variable declaration
                .filter { it.isGoingAfter(property.node) }
                .map { it.psi as KtNameReferenceExpression }
                .filter { it.getReferencedNameAsName() == property.nameAsName }
                .filterNot { expression ->
                    // to avoid false triggering on objects' fields with same name as property
                    isReferenceToFieldOfObject(expression) ||
                            // to exclude usages of local properties from other context (shadowed) and lambda arguments with same name
                            isReferenceToOtherVariableWithSameName(expression, this, property)
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
 *  going through all parent scopes from bottom to top until we will find the scope where the initial variable was declared
 *  all these scopes are on lower level of inheritance that's why if in one of these scopes we will find any
 *  variable declaration with the same name - we will understand that it is usage of another variable
*/
private fun isReferenceToOtherVariableWithSameName(expression: KtNameReferenceExpression, codeBlock: KtElement, property: KtProperty): Boolean {
    return expression.parents
            // getting all block expressions/class bodies/file node from bottom to the top
            // FixMe: Object companion is not resolved properly yet
            .filter { it is KtBlockExpression || it is KtClassBody || it is KtFile }
            // until we reached the block that contains the initial declaration
            .takeWhile { codeBlock != it }
            .any { block ->
                // this is not the expression that we needed if:
                //  1) there is a new shadowed declaration for this expression (but the declaration should stay on the previous line!)
                //  2) or there one of top blocks is a function/lambda that has arguments with the same name
                // FixMe: in class or a file the declaration can easily go after the usage (by lines of code)
                block.getChildrenOfType<KtProperty>().any { it.nameAsName == property.nameAsName && expression.node.isGoingAfter(it.node) } ||
                        block.parent
                                .let { it as? KtFunctionLiteral }
                                ?.valueParameters
                                ?.any { it.nameAsName == property.nameAsName }
                        ?: false
                // FixMe: also see very strange behavior of Kotlin in tests (disabled)
            }
}
