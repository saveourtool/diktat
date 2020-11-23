package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.EMPTY_PRIMARY_CONSTRUCTOR
import org.cqfn.diktat.ruleset.utils.getIdentifierName
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

        if (node.elementType == CLASS)
            checkCLass(node.psi as KtClass)
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkCLass(ktClass: KtClass) {
        if(ktClass.primaryConstructor?.valueParameters?.isNotEmpty() != false || ktClass.primaryConstructorModifierList != null)
            return
        if (ktClass.secondaryConstructors.isEmpty()) {
            warnOrFixOnEmptyPrimaryConstructor(ktClass.node, true) {
                ktClass.node.removeChild(ktClass.primaryConstructor!!.node)
            }
        } else {
            warnOrFixOnEmptyPrimaryConstructor(ktClass.node, false) {}
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun warnOrFixOnEmptyPrimaryConstructor(classNode: ASTNode, isFix: Boolean, autofix: () -> Unit) {
        EMPTY_PRIMARY_CONSTRUCTOR.warnAndFix(configRules, emitWarn, isFix, classNode.getIdentifierName()!!.text,
                classNode.startOffset, classNode){
            autofix()
        }
    }
}
