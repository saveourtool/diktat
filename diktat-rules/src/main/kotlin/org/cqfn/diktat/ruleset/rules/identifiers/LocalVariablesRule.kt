package org.cqfn.diktat.ruleset.rules.identifiers

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.isPartOfComment
import com.pinterest.ktlint.core.ast.lineNumber
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.LOCAL_VARIABLE_EARLY_DECLARATION
import org.cqfn.diktat.ruleset.rules.getDiktatConfigRules
import org.cqfn.diktat.ruleset.utils.containsOnlyConstants
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.findUsagesOf
import org.cqfn.diktat.ruleset.utils.getDeclarationScope
import org.cqfn.diktat.ruleset.utils.isContainingScope
import org.cqfn.diktat.ruleset.utils.lastLineNumber
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtFunctionLiteral
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.jetbrains.kotlin.psi.psiUtil.startOffset

/**
 * This rule checks that local variables are declared close to the point where they are first used.
 * Current algorithm assumes that scopes are always `BLOCK`s.
 * 1. Warns if there are statements between variable declaration and it's first usage
 * 2. It is allowed to declare variables in outer scope compared to usage scope. It could be useful to store state, e.g. between loop iterations.
 *
 * Current limitations due to usage of AST only:
 * * Only properties without initialization or initialized with expressions based on constants are supported.
 * * Properties initialized with constructor calls cannot be distinguished from method call and are no supported.
 */
class LocalVariablesRule : Rule("local-variables") {
    companion object {
        private var functionInitializers = listOf(
                "emptyList", "emptySet", "emptyMap", "emptyArray", "emptySequence",
                "listOf", "setOf", "mapOf", "arrayOf", "arrayListOf",
                "mutableListOf", "mutableSetOf", "mutableMapOf"
        )
    }

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

        if (node.elementType == FILE) {
            // collect all local properties and associate with corresponding references
            val propertiesToUsages = collectPropertiesWithUsages(node)

            // find all usages which include more than one property
            val multiPropertyUsages = propertiesToUsages
                    .mapValues { (_, usages) ->
                        usages.map { ref -> ref.parents.find { it.parent is KtBlockExpression }!! }
                    }
                    .mapValues { (_, statements) ->
                        val usageScopes = statements.map { it.getParentOfType<KtBlockExpression>(true)!! }
                        val outermostScope = findOutermost(usageScopes)
                        statements.getOrNull(usageScopes.indexOf(outermostScope))
                    }
                    .filterNot { it.value == null }
                    .map { it.value!! to it.key }
                    .groupByTo(mutableMapOf(), { it.first }) { it.second }
                    .filter { it.value.size > 1 }
                    .toMap<PsiElement, List<KtProperty>>()

            multiPropertyUsages
                    .forEach { (statement, properties) ->
                        // need to check that properties are declared consecutively with only maybe empty lines
                        properties
                                .sortedBy { it.node.lineNumber()!! }
                                .zip(properties.size - 1 downTo 0)
                                .forEach { (property, offset) ->
                                    checkLineNumbers(property, statement.node.lineNumber()!!, offset)
                                }
                    }

            propertiesToUsages
                    .filterNot { it.key in multiPropertyUsages.values.flatten() }
                    .forEach { handleLocalProperty(it.key, it.value) }
        }
    }

    private fun collectPropertiesWithUsages(node: ASTNode) = node
            .findAllNodesWithSpecificType(PROPERTY)
            .map { it.psi as KtProperty }
            .filter { it.isLocal && it.name != null && it.parent is KtBlockExpression }
            .filter {
                it.isVar && it.initializer == null ||
                        (it.initializer?.containsOnlyConstants() ?: false) ||
                        (it.initializer as? KtCallExpression).isWhitelistedMethod()
            }
            .associateWith(::findUsagesOf)
            .filterNot { it.value.isEmpty() }

    private fun handleLocalProperty(property: KtProperty, usages: List<KtNameReferenceExpression>) {
        val declarationScope = property.getDeclarationScope()

        val usageScopes = usages.mapNotNull { it.getParentOfType<KtBlockExpression>(true) }
        val outermostUsageScope = findOutermost(usageScopes)

        if (outermostUsageScope == declarationScope) {
            // property is declared in the same scope where it is used, we need to check how close it is to the first usage
            val firstUsage = usages.minBy { it.node.lineNumber()!! }!!
            val firstUsageScope = firstUsage.parents.find { it is KtBlockExpression }!!
                    .run {
                        if (this != declarationScope && parent is KtFunctionLiteral) {
                            parentsWithSelf.find {
                                it is KtBlockExpression && it.parent !is KtFunctionLiteral
                            }!!
                        } else {
                            this
                        }
                    }
            val firstUsageStatement = firstUsage.parents.find { it.parent == firstUsageScope }!!
            checkLineNumbers(property, firstUsageStatement.node.lineNumber()!!)
        } else {
            usageScopes
                    .first()
                    .parentsWithSelf
                    .filter { it is KtBlockExpression }
                    .zipWithNext()
                    .find { it.second == declarationScope }
                    ?.let { (usageScope, _) ->
                        val firstUsageLine = (usageScope.parent.takeIf { it is KtFunctionLiteral }
                                ?: usageScope).node.lineNumber()!!
                        checkLineNumbers(property, firstUsageLine)
                    }
        }
    }

    private fun checkLineNumbers(property: KtProperty, firstUsageLine: Int, offset: Int = 0) {
        val numLinesToSkip = property
                .siblings(forward = true, withItself = false)
                .takeWhile { it is PsiWhiteSpace || it.node.isPartOfComment() }
                .let { siblings -> siblings.last().node.lastLineNumber()!! - siblings.first().node.lineNumber()!! - 1 }

        if (firstUsageLine - numLinesToSkip != property.node.lastLineNumber()!! + 1 + offset) {
            LOCAL_VARIABLE_EARLY_DECLARATION.warn(configRules, emitWarn, isFixMode,
                    warnMessage(property.name!!, property.node.lineNumber()!!, firstUsageLine), property.startOffset)
        }
    }

    private fun KtCallExpression?.isWhitelistedMethod() =
            this?.run {
                (referenceExpression() as KtNameReferenceExpression).getReferencedName() in functionInitializers &&
                        valueArguments.isEmpty()
            } ?: false

    private fun findOutermost(scopes: List<KtBlockExpression>) = scopes.find { block ->
        scopes.all { block.isContainingScope(it) }
    }

    private fun warnMessage(name: String, declared: Int, used: Int) = "$name is declared on line $declared and used for the first time on line $used"
}
