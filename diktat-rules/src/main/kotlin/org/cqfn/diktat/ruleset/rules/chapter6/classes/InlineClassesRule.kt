package org.cqfn.diktat.ruleset.rules.chapter6.classes

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getCommonConfiguration
import org.cqfn.diktat.ruleset.constants.Warnings.INLINE_CLASS_CAN_BE_USED
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType

import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CONSTRUCTOR_CALLEE
import com.pinterest.ktlint.core.ast.ElementType.FINAL_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.INTERNAL_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.PRIVATE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PROTECTED_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.PUBLIC_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.SUPER_TYPE_LIST
import com.pinterest.ktlint.core.ast.ElementType.VAR_KEYWORD
import com.pinterest.ktlint.core.ast.children
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.psiUtil.visibilityModifierType

/**
 * This rule checks if inline class can be used.
 */
class InlineClassesRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(INLINE_CLASS_CAN_BE_USED)
) {
    override fun logic(node: ASTNode) {
        val configuration = configRules.getCommonConfiguration()
        if (node.elementType == CLASS &&
            !(node.psi as KtClass).isInterface() &&
            configuration.kotlinVersion >= minKtVersion &&
            configuration.kotlinVersion < maxKtVersion
        ) {
            handleClasses(node.psi as KtClass)
        }
    }

    private fun handleClasses(classPsi: KtClass) {
        // Fixme: In Kotlin 1.4.30 inline classes may be used with internal constructors. When it will be released need to check it
        if (hasValidProperties(classPsi) &&
            !isExtendingClass(classPsi.node) &&
            classPsi.node.getFirstChildWithType(MODIFIER_LIST)?.getChildren(null)?.all { it.elementType in goodModifiers } != false) {
            // Fixme: since it's an experimental feature we shouldn't do fixer
            INLINE_CLASS_CAN_BE_USED.warn(configRules, emitWarn, isFixMode, "class ${classPsi.name}", classPsi.node.startOffset, classPsi.node)
        }
    }

    private fun hasValidProperties(classPsi: KtClass): Boolean {
        if (classPsi.getProperties().size == 1 && !classPsi.hasExplicitPrimaryConstructor()) {
            return !classPsi.getProperties().single().isVar
        } else if (classPsi.getProperties().isEmpty() && classPsi.hasExplicitPrimaryConstructor()) {
            return classPsi.primaryConstructorParameters.size == 1 &&
                !classPsi.primaryConstructorParameters.first().node.hasChildOfType(VAR_KEYWORD) &&
                classPsi.primaryConstructor?.visibilityModifierType()?.value?.let { it == "public" } ?: true
        }
        return false
    }

    private fun isExtendingClass(node: ASTNode): Boolean =
        node
            .getFirstChildWithType(SUPER_TYPE_LIST)
            ?.children()
            ?.any { it.hasChildOfType(CONSTRUCTOR_CALLEE) }
            ?: false

    companion object {
        const val NAME_ID = "aaw-inline-classes"
        val minKtVersion = KotlinVersion(1, 3)
        val maxKtVersion = KotlinVersion(1, 5, 0)
        val goodModifiers = listOf(PUBLIC_KEYWORD, PRIVATE_KEYWORD, FINAL_KEYWORD, PROTECTED_KEYWORD, INTERNAL_KEYWORD)
    }
}
