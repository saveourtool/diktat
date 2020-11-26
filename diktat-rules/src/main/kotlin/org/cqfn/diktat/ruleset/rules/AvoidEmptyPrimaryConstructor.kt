package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.EMPTY_PRIMARY_CONSTRUCTOR

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass

class AvoidEmptyPrimaryConstructor(private val configRules: List<RulesConfig>) : Rule("avoid-empty-primary-constructor") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == CLASS) {
            checkClass(node.psi as KtClass)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkClass(ktClass: KtClass) {
        if (ktClass.primaryConstructor?.valueParameters?.isNotEmpty() != false || ktClass.primaryConstructorModifierList != null) {
            return
        }
        EMPTY_PRIMARY_CONSTRUCTOR.warnAndFix(configRules, emitWarn, isFixMode, ktClass.nameIdentifier!!.text,
            ktClass.node.startOffset, ktClass.node) {
            ktClass.node.removeChild(ktClass.primaryConstructor!!.node)
        }
    }
}
