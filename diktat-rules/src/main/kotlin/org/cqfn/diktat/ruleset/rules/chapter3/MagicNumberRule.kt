package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getCommonConfiguration
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.MAGIC_NUMBER
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.ast.ElementType.BINARY_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.ENUM_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.FLOAT_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.MINUS
import com.pinterest.ktlint.core.ast.ElementType.OPERATION_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.RANGE
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.core.ast.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.isExtensionDeclaration
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Rule for magic number
 */
class MagicNumberRule(configRules: List<RulesConfig>) : DiktatRule(
   nameId,
    configRules,
    listOf(MAGIC_NUMBER)
) {
    private val configuration by lazy {
        MagicNumberConfiguration(
            configRules.getRuleConfig(MAGIC_NUMBER)?.configuration ?: emptyMap()
        )
    }
    @Suppress("COLLAPSE_IF_STATEMENTS")
    override fun logic(node: ASTNode) {
        val filePath = node.getFilePath()
        val config = configRules.getCommonConfiguration()
        if (node.elementType == INTEGER_CONSTANT || node.elementType == FLOAT_CONSTANT) {
            if (!isLocatedInTest(filePath.splitPathToDirs(), config.testAnchors) || !configuration.isIgnoreTest) {
                checkNumber(node, configuration)
            }
        }
    }

    @Suppress("ComplexMethod")
    private fun checkNumber(node: ASTNode, configuration: MagicNumberConfiguration) {
        val nodeText = node.treePrev?.let { if (it.elementType == OPERATION_REFERENCE && it.hasChildOfType(MINUS)) "-${node.text}" else node.text } ?: node.text
        val isIgnoreNumber = configuration.ignoreNumbers.contains(nodeText)
        val isHashFunction = node.parent({ it.elementType == FUN && it.isHashFun() }) != null
        val isConstant = node.parent({ it.elementType == PROPERTY && it.isConstant() }) != null
        val isPropertyDeclaration = !isConstant && node.parent({ it.elementType == PROPERTY && !it.isNodeFromCompanionObject() }) != null
        val isLocalVariable = node.parent({ it.elementType == PROPERTY && it.isVarProperty() && (it.psi as KtProperty).isLocal }) != null
        val isValueParameter = node.parent({ it.elementType == VALUE_PARAMETER }) != null
        val isCompanionObjectProperty = node.parent({ it.elementType == PROPERTY && it.isNodeFromCompanionObject() }) != null
        val isEnums = node.parent({ it.elementType == ENUM_ENTRY }) != null
        val isRanges = node.treeParent.run {
            this.elementType == BINARY_EXPRESSION &&
                    this.findChildByType(OPERATION_REFERENCE)?.hasChildOfType(RANGE) ?: false
        }
        val isExtensionFunctions = node.parent({ it.elementType == FUN && (it.psi as KtFunction).isExtensionDeclaration() }) != null &&
                node.parents().find { it.elementType == PROPERTY } == null
        val result = listOf(isHashFunction, isPropertyDeclaration, isLocalVariable, isValueParameter, isConstant,
            isCompanionObjectProperty, isEnums, isRanges, isExtensionFunctions).zip(mapConfiguration.map { configuration.getParameter(it.key) })
        if (result.any { it.first && it.first != it.second } && !isIgnoreNumber) {
            MAGIC_NUMBER.warn(configRules, emitWarn, isFixMode, nodeText, node.startOffset, node)
        }
    }

    private fun ASTNode.isHashFun() =
            (this.psi as KtFunction).run {
                this.nameIdentifier?.text == "hashCode" && this.annotationEntries.map { it.text }.contains("@Override")
            }

    /**
     * [RuleConfiguration] for configuration
     */
    class MagicNumberConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Flag to ignore numbers from test
         */
        val isIgnoreTest = config["ignoreTest"]?.toBoolean() ?: IGNORE_TEST

        /**
         * List of ignored numbers
         */
        val ignoreNumbers = config["ignoreNumbers"]?.split(",")?.map { it.trim() }?.filter { it.isNumber() } ?: ignoreNumbersList

        /**
         * @param param parameter from config
         * @return value of parameter
         */
        @Suppress("UnsafeCallOnNullableType")
        fun getParameter(param: String) = config[param]?.toBoolean() ?: mapConfiguration[param]!!

        /**
         * Check if string is number
         */
        private fun String.isNumber() = (this.toLongOrNull() ?: this.toFloatOrNull()) != null
    }

    companion object {
        const val IGNORE_TEST = true
        val nameId = "aca-magic-number"
        val ignoreNumbersList = listOf("-1", "1", "0", "2")
        val mapConfiguration = mapOf(
            "ignoreHashCodeFunction" to true,
            "ignorePropertyDeclaration" to false,
            "ignoreLocalVariableDeclaration" to false,
            "ignoreValueParameter" to true,
            "ignoreConstantDeclaration" to true,
            "ignoreCompanionObjectPropertyDeclaration" to true,
            "ignoreEnums" to false,
            "ignoreRanges" to false,
            "ignoreExtensionFunctions" to false)
    }
}
