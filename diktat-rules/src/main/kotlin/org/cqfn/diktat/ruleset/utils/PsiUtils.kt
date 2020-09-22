package org.cqfn.diktat.ruleset.utils

import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstantExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtReferenceExpression
import org.jetbrains.kotlin.psi.KtStringTemplateExpression
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isAncestor
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression

/**
 * Checks if this [KtExpression] contains only constant literals, strings with possibly constant expressions in templates,
 * method calls on literals.
 */
@Suppress("UnsafeCallOnNullableType")
fun KtExpression.containsOnlyConstants(): Boolean =
        when (this) {
            is KtConstantExpression -> true
            is KtStringTemplateExpression -> entries.all { it.expression?.containsOnlyConstants() ?: true }
            // left and right are marked @Nullable @IfNotParsed, so it should be safe to `!!`
            is KtBinaryExpression -> left!!.containsOnlyConstants() && right!!.containsOnlyConstants()
            is KtDotQualifiedExpression -> receiverExpression.containsOnlyConstants() &&
                    (selectorExpression is KtReferenceExpression ||
                            ((selectorExpression as? KtCallExpression)
                                    ?.valueArgumentList
                                    ?.arguments
                                    ?.all { it.getArgumentExpression()!!.containsOnlyConstants() }
                                    ?: false)
                            )
            else -> false
        }

/**
 * Get block inside which the property is declared.
 * Here we assume that property can be declared only in block, since declaration is not an expression in kotlin
 * and compiler prohibits things like `if (condition) val x = 0`.
 */
@Suppress("UnsafeCallOnNullableType")
fun KtProperty.getDeclarationScope() = getParentOfType<KtBlockExpression>(true)!!
        .let { if (it is KtIfExpression) it.then!! else it }
        .let { if (it is KtTryExpression) it.tryBlock else it }
        as KtBlockExpression

/**
 * Finds all references to [property] in the same code block.
 * @return list of references as [KtNameReferenceExpression]
 */
fun findUsagesOf(property: KtProperty) = property
        .getDeclarationScope()
        .let { declarationScope ->
            val name = property.nameAsName
            declarationScope
                    .node
                    .findAllNodesWithSpecificType(ElementType.REFERENCE_EXPRESSION)
                    .map { it.psi as KtNameReferenceExpression }
                    .filter { it.getReferencedNameAsName() == name }
                    .filterNot {
                        // to avoid false triggering on objects' fields with same name as local property
                        (it.parent as? KtDotQualifiedExpression)?.run {
                            receiverExpression != it && selectorExpression?.referenceExpression() == it
                        } ?: false
                    }
                    .filterNot { ref ->
                        // to exclude usages of local properties and lambda arguments with same name
                        ref.parents.mapNotNull { it as? KtBlockExpression }.takeWhile { it != declarationScope }.any { block ->
                            block.getChildrenOfType<KtProperty>().any { it.nameAsName == name } ||
                                    (block.parent.let { it as? KtFunctionLiteral }?.valueParameters?.any { it.nameAsName == name }
                                            ?: false)
                        }
                    }
        }

/**
 * Checks if this [PsiElement] is an ancestor of [block].
 * Nodes like `IF`, `TRY` are parents of `ELSE`, `CATCH`, but their scopes are not intersecting, and false is returned in this case.
 */
fun PsiElement.isContainingScope(block: KtBlockExpression): Boolean {
    when (block.parent.node.elementType) {
        ElementType.ELSE -> getParentOfType<KtIfExpression>(true)
        ElementType.CATCH -> getParentOfType<KtTryExpression>(true)
        else -> null
    }.let {
        if (this == it) return false
    }
    return isAncestor(block, false)
}
