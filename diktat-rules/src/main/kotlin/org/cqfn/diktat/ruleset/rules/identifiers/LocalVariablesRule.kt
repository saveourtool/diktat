package org.cqfn.diktat.ruleset.rules.identifiers

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.CATCH
import com.pinterest.ktlint.core.ast.ElementType.ELSE
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.lineNumber
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.LOCAL_VARIABLE_EARLY_DECLARATION
import org.cqfn.diktat.ruleset.rules.getDiktatConfigRules
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.firstLineOfText
import org.cqfn.diktat.ruleset.utils.numNewLines
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLoopExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.KtTryExpression
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isAncestor
import org.jetbrains.kotlin.psi.psiUtil.parents
import kotlin.math.max

/**
 * This rule checks that local variables are declared close to the point where they are first used.
 * @implNote current algorithm assumes that scopes are always `BLOCK`s
 * TODO: handle case when several variables are declared on consecutive lines and then used all at once
 * ```
 *     val a = ..
 *     val b = ..
 *     foo(a, b)
 * ```
 */
class LocalVariablesRule : Rule("local-variables") {
    private lateinit var configRules: List<RulesConfig>
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       params: KtLint.Params,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        configRules = params.getDiktatConfigRules()
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == PROPERTY) {
            if (node.treeParent.elementType != BLOCK) {
                return
            }

            (node.psi as KtProperty)
                    .takeIf { it.isLocal }
                    ?.let(::handleLocalProperty)
        }
    }

    private fun handleLocalProperty(property: KtProperty) {
        val name = property.nameAsName
                ?: return

        val declarationScope = property.getParentOfType<KtBlockExpression>(true)!!

        val usages = property.getParentOfType<KtBlockExpression>(true)!!
                .let { if (it is KtIfExpression) it.then!! else it }
                .let { if (it is KtTryExpression) it.tryBlock else it }
                .node
                .findAllNodesWithSpecificType(REFERENCE_EXPRESSION)
                .map { it.psi as KtNameReferenceExpression }
                .filter { it.getIdentifier()!!.text == name.identifier }
                .filter {
                    // to avoid false triggering on objects' fields with same name as local property
                    it.parent !is KtDotQualifiedExpression
                }
                .filterNot { ref ->
                    // to exclude usages of local properties and lambda arguments with same name
                    ref.parents.filter { it is KtBlockExpression }.takeWhile { it != declarationScope }.any { block ->
                        block.getChildrenOfType<KtProperty>().any { it.nameAsName == name } ||
                                (block.parent.let { it as? KtFunctionLiteral }?.valueParameters?.any { it.nameAsName == name } ?: false)
                    }
                }

        if (usages.isEmpty()) {
            return
        }

        val usageScopes = usages.mapNotNull { it.getParentOfType<KtBlockExpression>(true) }
        val outermostUsageScope = usageScopes.find { block ->
            usageScopes.all { block.isContainingScope(it) }
        }

        // properties are permitted outside of loops, they could need to store data between iterations
        val needsToBeDeclaredOutside = outermostUsageScope?.parent?.parent is KtLoopExpression
        val node = property.node

        if (outermostUsageScope == declarationScope) {
            // property is declared in the same scope where it is used, we need to check how close it is to the first usage

            // todo write test where just usages.firstOrNull() is not enough. Or is ut OK, because findAllNodes operates as DFS?
            val firstUsage = usages.minBy { it.node.lineNumber()!! }!!

            // treat last line as declaration line for multiline declarations
            val effectiveDeclarationLine = property.node.lineNumber()!! + property.text.lines().size - 1

            // treat usage line as first line of usage statement for multiline usages
            val effectiveFirstUsageLine = firstUsage.parents
                    .find { it.parent is KtBlockExpression }!!
                    .node
                    .lineNumber()!! -
                    // trim blank lines between declaration and first usage
                    max(0, node.treeNext.numNewLines() - 1)
            if (effectiveFirstUsageLine != effectiveDeclarationLine + 1) {
                LOCAL_VARIABLE_EARLY_DECLARATION.warn(configRules, emitWarn, isFixMode, node.firstLineOfText("..."), node.startOffset)
            }
        } else if (false) {
            // this branch is TODO
            // Here declaration is in outer scope compared to any of it's usages; sometimes it means declaration can be moved deeper.
            // But, e.g., for loops it is not true.
            if (outermostUsageScope != null) {
                if (needsToBeDeclaredOutside && outermostUsageScope.getParentOfType<KtBlockExpression>(true) != declarationScope ||
                        !needsToBeDeclaredOutside) {
                    LOCAL_VARIABLE_EARLY_DECLARATION.warn(configRules, emitWarn, isFixMode, node.firstLineOfText("..."), node.startOffset)
                }
            } else {
                val outermostScope = usageScopes
                        .first()
                        .parents
                        .filter { it is KtBlockExpression }
                        .find { block ->
                            usageScopes.all { block.isContainingScope(it) }
                        }
                if (outermostScope != declarationScope) {
                    LOCAL_VARIABLE_EARLY_DECLARATION.warn(configRules, emitWarn, isFixMode, node.firstLineOfText("..."), node.startOffset)
                }
            }
        }
    }

    /**
     * Checks if this [PsiElement] is an ancestor of [block].
     * Nodes like `IF`, `TRY` are parents of `ELSE`, `CATCH`, but their scopes are not intersecting, and false is returned in this case.
     */
    private fun PsiElement.isContainingScope(block: KtBlockExpression): Boolean {
        when (block.parent.node.elementType) {
            ELSE -> getParentOfType<KtIfExpression>(true)
            CATCH -> getParentOfType<KtTryExpression>(true)
            else -> null
        }.let {
            if (this == it) return false
        }
        return isAncestor(block, false)
    }
}
