package org.cqfn.diktat.ruleset.rules.classes

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.isPartOfComment
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.jetbrains.kotlin.psi.psiUtil.startOffset

class CompactInitialization(private val configRules: List<RulesConfig>) : Rule("class-compact-initialization") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode, autoCorrect: Boolean, emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        node.psi
            .let { it as? KtProperty }
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
                .map { it to ((it as KtBinaryExpression).left as KtDotQualifiedExpression).selectorExpression!! }
        }
            .toList()
            .forEach { (assignment, field) ->
                Warnings.COMPACT_OBJECT_INITIALIZATION.warnAndFix(configRules, emitWarn, isFixMode, field.text,
                    assignment.startOffset, assignment.node) {
                    TODO()
                }
            }
    }
}
