package org.cqfn.diktat.ruleset.rules.classes

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.isPartOfComment
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.ruleset.utils.getFunctionName
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.jetbrains.kotlin.psi.psiUtil.startOffset

class CompactInitialization(private val configRules: List<RulesConfig>) : Rule("class-compact-initialization") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false
    private val kotlinParser by lazy { KotlinParser() }

    override fun visit(node: ASTNode, autoCorrect: Boolean, emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        node.psi
            .let { it as? KtProperty }
            ?.takeIf { it.hasInitializer() }
            ?.let(::handleProperty)
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleProperty(property: KtProperty) {
        property.run {
            val propertyName = name
            siblings(forward = true, withItself = false)
                .filterNot { it.node.isPartOfComment() || it is PsiWhiteSpace }
                .takeWhile {
                    it is KtBinaryExpression && (it.left as? KtDotQualifiedExpression)?.run {
                        (receiverExpression as? KtNameReferenceExpression)?.getReferencedName() == propertyName
                    }
                            ?: false
                }
                .map { it as KtBinaryExpression to (it.left as KtDotQualifiedExpression).selectorExpression!! }
        }
            .toList()
            .forEach { (assignment, field) ->
                Warnings.COMPACT_OBJECT_INITIALIZATION.warnAndFix(
                    configRules, emitWarn, isFixMode, field.text,
                    assignment.startOffset, assignment.node
                ) {
                    moveAssignmentIntoApply(property, assignment)
                }
            }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun moveAssignmentIntoApply(property: KtProperty, assignment: KtBinaryExpression) {
        val applyExpression = getOrCreateApplyBlock(property)
        applyExpression.run {
            lambdaArguments.singleOrNull()
                ?.run {
                    getLambdaExpression()?.run {
                        val bodyExpression = functionLiteral
                            // note: we are dealing with function literal: braces belong to KtFunctionLiteral, but it's body is a KtBlockExpression
                            // which therefore doesn't have braces
                            .bodyExpression!!
                            .node
                        assignment.node.siblings(forward = false)
                            .takeWhile { it != property.node }
                            .toList()
                            .reversed()
                            .forEach {
                                bodyExpression.addChild(it.clone() as ASTNode, null)
                                it.treeParent.removeChild(it)
                            }
                        bodyExpression.addChild(kotlinParser.createNode(assignment.text.substringAfter('.')), null)
                        assignment.node.run { treeParent.removeChild(this) }
                    } ?: run {
                        // todo
                    }
                }
                ?: run {
                    // this is apply like `apply(::foo), this branch is todo
//                    valueArguments
                }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun getOrCreateApplyBlock(property: KtProperty): KtCallExpression = (property.initializer as? KtDotQualifiedExpression)?.selectorExpression
        ?.let { it as? KtCallExpression }?.takeIf { it.getFunctionName() == "apply" } ?: run {
        // add apply block
        property.node.run {
            val newInitializerNode = kotlinParser.createNode("${property.initializer!!.text}.apply {}")
            replaceChild(property.initializer!!.node, newInitializerNode)
        }
        (property.initializer as KtDotQualifiedExpression).selectorExpression!! as KtCallExpression
    }
}
