package org.cqfn.diktat.ruleset.rules.chapter3

import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.ENUM_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FLOAT_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.parent
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.MAGIC_NUMBER
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.isConstant
import org.cqfn.diktat.ruleset.utils.isNodeFromCompanionObject
import org.cqfn.diktat.ruleset.utils.isVarProperty
import org.cqfn.diktat.ruleset.utils.prettyPrint
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.isExtensionDeclaration
import org.jetbrains.kotlin.psi.psiUtil.startOffset

/**
 * Litaral and constant
 */
class MagicNumberRule(configRules: List<RulesConfig>) : DiktatRule("magic-number", configRules, listOf(MAGIC_NUMBER)) {
    override fun logic(node: ASTNode) {
        val configuration = MagicNumberConfiguration(
            configRules.getRuleConfig(MAGIC_NUMBER)?.configuration ?: emptyMap()
        )
        if (node.elementType == INTEGER_CONSTANT || node.elementType == FLOAT_CONSTANT){
            checkNumber(node, configuration)
        }
    }

    private fun checkNumber(node: ASTNode, configuration: MagicNumberConfiguration) {
        val isIgnoreNumber = configuration.ignoreNumbers.contains(node.text.toInt())
        val isPropertyDeclaration = node.parent({it.elementType == PROPERTY}) != null
        val isLocalVariable = node.parent({it.isVarProperty() && (it.psi as KtProperty).isLocal}) != null
        val isConstant = node.parent({it.isConstant()}) != null
        val isCompanionObjectProperty = node.parent({it.elementType == PROPERTY && it.isNodeFromCompanionObject()}) != null
        val isEnums = node.parent({it.elementType == ENUM_ENTRY}) != null
        val isExtensionFunctions = node.parent({it.elementType == FUN && (it.psi as KtFunction).isExtensionDeclaration()}) != null

        val allList = listOf(isIgnoreNumber, isPropertyDeclaration, isLocalVariable, isConstant,
            isCompanionObjectProperty, isEnums, isExtensionFunctions)

        val result = allList.zip(mapConfiguration.map {configuration.getParameter(it.key)})

        if(result.all {it.first != it.second}) {
            MAGIC_NUMBER.warn(configRules, emitWarn, isFixMode, node.text, node.startOffset, node)
        }
    }

    /**
     * [RuleConfiguration] for configuration
     */
    class MagicNumberConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Ignore numbers
         */
        val ignoreNumbers = config["ignoreNumbers"]?.split(",")?.map { it.trim().toInt() } ?: ignoreNumbersList

        //val ignoreRange = config["ignoreRange"] ?: LongRange.EMPTY

        fun getParameter(param: String): Boolean = config[param]?.toBoolean() ?: mapConfiguration[param]!!

    }

    companion object {
        val ignoreNumbersList = listOf(-1, 1, 0, 2)
        val mapConfiguration  = mapOf("ignoreHashCodeFunction" to true,
            "ignorePropertyDeclaration" to false,
            "ignoreLocalVariableDeclaration" to false,
            "ignoreConstantDeclaration" to true,
            "ignoreCompanionObjectPropertyDeclaration" to true,
            "ignoreEnums" to false,
            "ignoreExtensionFunctions" to false,)
    }
}
