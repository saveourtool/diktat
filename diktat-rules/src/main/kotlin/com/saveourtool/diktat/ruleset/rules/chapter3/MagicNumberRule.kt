package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getCommonConfiguration
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.MAGIC_NUMBER
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*
import com.saveourtool.diktat.ruleset.utils.parent

import org.jetbrains.kotlin.KtNodeTypes.BINARY_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.ENUM_ENTRY
import org.jetbrains.kotlin.KtNodeTypes.FLOAT_CONSTANT
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.INTEGER_CONSTANT
import org.jetbrains.kotlin.KtNodeTypes.OPERATION_REFERENCE
import org.jetbrains.kotlin.KtNodeTypes.PROPERTY
import org.jetbrains.kotlin.KtNodeTypes.VALUE_PARAMETER
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.lexer.KtTokens.MINUS
import org.jetbrains.kotlin.lexer.KtTokens.RANGE
import org.jetbrains.kotlin.psi.KtFunction
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.isExtensionDeclaration
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Rule for magic number
 */
class MagicNumberRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
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

        val isHashFunction = node.parent { it.elementType == FUN && it.isHashFun() } != null
        val isLocalVariable = node.parent { it.elementType == PROPERTY && (it.isVarProperty() || it.isValProperty()) && (it.psi as KtProperty).isLocal } != null
        val isValueParameter = node.parent { it.elementType == VALUE_PARAMETER } != null
        val isConstant = node.parent { it.elementType == PROPERTY && it.isConstant() } != null
        val isCompanionObjectProperty = node.parent { it.elementType == PROPERTY && it.isNodeFromCompanionObject() } != null
        val isEnums = node.parent { it.elementType == ENUM_ENTRY } != null
        val isRanges = node.treeParent.let {
            it.elementType == BINARY_EXPRESSION && it.findChildByType(OPERATION_REFERENCE)?.hasChildOfType(RANGE) ?: false
        }
        val isExtensionFunctions = node.parent { it.elementType == FUN && (it.psi as KtFunction).isExtensionDeclaration() } != null &&
                node.parents().none { it.elementType == PROPERTY }
        val isPairsCreatedUsingTo = node.treeParent.let {
            it.elementType == BINARY_EXPRESSION && it.findChildByType(OPERATION_REFERENCE)?.findChildByType(IDENTIFIER)?.text == "to"
        }
        val isPropertyDeclaration = !isLocalVariable && !isConstant && !isCompanionObjectProperty && !isRanges && !isPairsCreatedUsingTo &&
                node.parent { it.elementType == PROPERTY } != null

        val result = listOf(isHashFunction, isPropertyDeclaration, isLocalVariable, isValueParameter, isConstant, isCompanionObjectProperty, isEnums, isRanges,
            isExtensionFunctions, isPairsCreatedUsingTo).zip(mapConfiguration.map { configuration.getParameter(it.key) })

        if (result.any { it.first && !it.second } && !isIgnoreNumber) {
            MAGIC_NUMBER.warn(configRules, emitWarn, nodeText, node.startOffset, node)
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
        val ignoreNumbers = config["ignoreNumbers"]?.split(",")?.map { it.trim() }?.filter { it.isNumber() || it.isOtherNumberType() } ?: ignoreNumbersList

        /**
         * @param param parameter from config
         * @return value of parameter
         */
        @Suppress("UnsafeCallOnNullableType")
        fun getParameter(param: String) = config[param]?.toBoolean() ?: mapConfiguration[param]!!

        /**
         * Check if string is number
         */
        // || ((this.last().uppercase() == "L" || this.last().uppercase() == "U") && this.substring(0, this.lastIndex-1).isNumber())
        private fun String.isNumber() = (this.toLongOrNull() ?: this.toFloatOrNull()) != null

        /**
         * Check if string include a char of number type
         */
        private fun String.isOtherNumberType(): Boolean = ((this.last().uppercase() == "L" || this.last().uppercase() == "U") && this.substring(0, this.lastIndex).isNumber()) ||
                (this.substring(this.lastIndex - 1).uppercase() == "UL" && this.substring(0, this.lastIndex - 1).isNumber())
    }

    companion object {
        const val IGNORE_TEST = true
        const val NAME_ID = "magic-number"
        val ignoreNumbersList = listOf("-1", "1", "0", "2", "0U", "1U", "2U", "-1L", "0L", "1L", "2L", "0UL", "1UL", "2UL")
        val mapConfiguration = mapOf(
            "ignoreHashCodeFunction" to true,
            "ignorePropertyDeclaration" to false,
            "ignoreLocalVariableDeclaration" to false,
            "ignoreValueParameter" to true,
            "ignoreConstantDeclaration" to true,
            "ignoreCompanionObjectPropertyDeclaration" to true,
            "ignoreEnums" to false,
            "ignoreRanges" to false,
            "ignoreExtensionFunctions" to false,
            "ignorePairsCreatedUsingTo" to false)
    }
}
