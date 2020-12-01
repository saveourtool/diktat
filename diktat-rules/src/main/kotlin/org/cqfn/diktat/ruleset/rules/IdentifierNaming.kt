package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.CATCH
import com.pinterest.ktlint.core.ast.ElementType.CATCH_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.DESTRUCTURING_DECLARATION
import com.pinterest.ktlint.core.ast.ElementType.DESTRUCTURING_DECLARATION_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_TYPE
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.TYPE_PARAMETER
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.core.ast.prevCodeSibling
import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings.BACKTICKS_PROHIBITED
import org.cqfn.diktat.ruleset.constants.Warnings.CLASS_NAME_INCORRECT
import org.cqfn.diktat.ruleset.constants.Warnings.CONSTANT_UPPERCASE
import org.cqfn.diktat.ruleset.constants.Warnings.ENUM_VALUE
import org.cqfn.diktat.ruleset.constants.Warnings.EXCEPTION_SUFFIX
import org.cqfn.diktat.ruleset.constants.Warnings.FUNCTION_BOOLEAN_PREFIX
import org.cqfn.diktat.ruleset.constants.Warnings.FUNCTION_NAME_INCORRECT_CASE
import org.cqfn.diktat.ruleset.constants.Warnings.GENERIC_NAME
import org.cqfn.diktat.ruleset.constants.Warnings.IDENTIFIER_LENGTH
import org.cqfn.diktat.ruleset.constants.Warnings.CONFUSING_IDENTIFIER_NAMING
import org.cqfn.diktat.ruleset.constants.Warnings.OBJECT_NAME_INCORRECT
import org.cqfn.diktat.ruleset.constants.Warnings.VARIABLE_HAS_PREFIX
import org.cqfn.diktat.ruleset.constants.Warnings.VARIABLE_NAME_INCORRECT
import org.cqfn.diktat.ruleset.constants.Warnings.VARIABLE_NAME_INCORRECT_FORMAT
import org.cqfn.diktat.ruleset.utils.checkLength
import org.cqfn.diktat.ruleset.utils.containsOneLetterOrZero
import org.cqfn.diktat.ruleset.utils.findChildAfter
import org.cqfn.diktat.ruleset.utils.findLeafWithSpecificType
import org.cqfn.diktat.ruleset.utils.findParentNodeWithSpecificType
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.getIdentifierName
import org.cqfn.diktat.ruleset.utils.getTypeParameterList
import org.cqfn.diktat.ruleset.utils.hasPrefix
import org.cqfn.diktat.ruleset.utils.hasTestAnnotation
import org.cqfn.diktat.ruleset.utils.isConstant
import org.cqfn.diktat.ruleset.utils.isDigits
import org.cqfn.diktat.ruleset.utils.isLowerCamelCase
import org.cqfn.diktat.ruleset.utils.isOverriden
import org.cqfn.diktat.ruleset.utils.isPascalCase
import org.cqfn.diktat.ruleset.utils.isUpperSnakeCase
import org.cqfn.diktat.ruleset.utils.removePrefix
import org.cqfn.diktat.ruleset.utils.toLowerCamelCase
import org.cqfn.diktat.ruleset.utils.toPascalCase
import org.cqfn.diktat.ruleset.utils.toUpperSnakeCase
import org.cqfn.diktat.ruleset.utils.Style
import org.jetbrains.kotlin.builtins.PrimitiveType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * This visitor covers rules:  1.2, 1.3, 1.4, 1.5 of Huawei code style. It covers following rules:
 * 1) All identifiers should use only ASCII letters or digits, and the names should match regular expressions \w{2,64}
 *  exceptions: variables like i,j,k
 * 2) constants from companion object should have UPPER_SNAKE_CASE
 * 3) fields/variables should have lowerCamelCase and should not contain prefixes
 * 4) interfaces/classes/annotations/enums/object names should be in PascalCase
 * 5) methods: function names should be in camel case, methods that return boolean value should have "is"/"has" prefix
 * 6) custom exceptions: PascalCase and Exception suffix
 * 7) FixMe: should prohibit identifiers with free format with `` (except test functions)
 *
 * // FixMe: very important, that current implementation cannot fix identifier naming properly,
 * // FixMe: because it fixes only declaration without the usages
 */
@Suppress("ForbiddenComment")
class IdentifierNaming(private val configRules: List<RulesConfig>) : Rule("identifier-naming") {

    companion object {
        // FixMe: this should be moved to properties
        val ONE_CHAR_IDENTIFIERS = setOf("i", "j", "k", "x", "y", "z")
        val BOOLEAN_METHOD_PREFIXES = setOf("has", "is")
        val CONFUSING_IDENTIFIER_NAMES = setOf("O", "D", "I", "l", "Z", "S", "e", "B", "h", "n", "m", "rn")
        const val MAX_IDENTIFIER_LENGTH = 64
        const val MIN_IDENTIFIER_LENGTH = 2

    }

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(
            node: ASTNode,
            autoCorrect: Boolean,
            emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit
    ) {
        isFixMode = autoCorrect
        emitWarn = emit

        // backticks are prohibited for identifier declarations everywhere except test methods that are marked with @Test annotation
        if (isIdentifierWithBackticks(node)) {
            return
        }

        // isVariable is used as a workaround to check corner case with variables that have length == 1
        val (identifierNodes, isVariable) = when (node.elementType) {
            // covers interface, class, enum class and annotation class names
            ElementType.CLASS -> Pair(checkCLassNamings(node), false)
            // covers "object" code blocks
            ElementType.OBJECT_DECLARATION -> Pair(checkObjectNaming(node), false)
            // covers variables (val/var), constants (const val) and parameters for lambdas
            ElementType.PROPERTY, ElementType.VALUE_PARAMETER -> Pair(checkVariableName(node), true)
            // covers case of enum values
            ElementType.ENUM_ENTRY -> Pair(checkEnumValues(node), false)
            // covers global functions, extensions and class methods
            ElementType.FUN -> Pair(checkFunctionName(node), false)
            else -> Pair(null, false)
        }

        if (identifierNodes != null) {
            checkIdentifierLength(identifierNodes, isVariable)
        }
    }

    /**
     * method checks that identifier is wrapped over with backticks (``)
     */
    private fun isIdentifierWithBackticks(node: ASTNode): Boolean {
        val identifier = node.getIdentifierName()
        if (identifier != null && node.elementType != REFERENCE_EXPRESSION) {
            // node is a symbol declaration with present identifier
            val identifierText = identifier.text
            if (identifierText.startsWith('`') && identifierText.endsWith('`')) {
                // the only exception is test method with @Test annotation
                if (!(node.elementType == ElementType.FUN && node.hasTestAnnotation())) {
                    BACKTICKS_PROHIBITED.warn(configRules, emitWarn, isFixMode, identifierText, identifier.startOffset, identifier)
                }
                return true
            }
        }

        return false
    }

    /**
     * all checks for case and naming for vals/vars/constants
     */
    private fun checkVariableName(node: ASTNode): List<ASTNode> {
        // special case for Destructuring declarations that can be treated as parameters in lambda:
        var namesOfVariables = extractVariableIdentifiers(node)
        namesOfVariables
                .forEach { variableName ->
                    // variable should not contain only one letter in it's name. This is a bad example: b512
                    // but no need to raise a warning here if length of a variable. In this case we will raise IDENTIFIER_LENGTH
                    if (variableName.text.containsOneLetterOrZero() && variableName.text.length > 1) {
                        VARIABLE_NAME_INCORRECT.warn(configRules, emitWarn, isFixMode, variableName.text, variableName.startOffset, node)
                    }
                    // check if identifier of a property has a confusing name
                    if (CONFUSING_IDENTIFIER_NAMES.contains(variableName.text) && !validCatchIdentifier(variableName) &&
                            node.elementType == ElementType.PROPERTY) {
                        warnConfusingName(variableName)
                    }
                    // check for constant variables - check for val from companion object or on global file level
                    // it should be in UPPER_CASE, no need to raise this warning if it is one-letter variable
                    if (node.isConstant()) {
                        if (!variableName.text.isUpperSnakeCase() && variableName.text.length > 1) {
                            CONSTANT_UPPERCASE.warnAndFix(configRules, emitWarn, isFixMode, variableName.text, variableName.startOffset,node) {
                                (variableName as LeafPsiElement).replaceWithText(variableName.text.toUpperSnakeCase())
                            }
                        }
                    } else if (variableName.text != "_" && !variableName.text.isLowerCamelCase()) {
                        // variable name should be in camel case. The only exception is a list of industry standard variables like i, j, k.
                        VARIABLE_NAME_INCORRECT_FORMAT.warnAndFix(configRules, emitWarn, isFixMode, variableName.text, variableName.startOffset, node) {
                            // FixMe: cover fixes with tests
                            (variableName as LeafPsiElement).replaceWithText(variableName.text.toLowerCamelCase())
                        }
                    }
                }

        // need to get new node in case we have already converted the case before (and replaced the child node)
        // we need to recalculate it twice, because nodes could have been changed by "replaceWithText" function
        namesOfVariables = extractVariableIdentifiers(node)
        namesOfVariables
                .forEach { variableName ->
                    // generally, variables with prefixes are not allowed (like mVariable, xCode, iValue)
                    if (variableName.text.hasPrefix()) {
                        VARIABLE_HAS_PREFIX.warnAndFix(configRules, emitWarn, isFixMode, variableName.text, variableName.startOffset, node) {
                            (variableName as LeafPsiElement).replaceWithText(variableName.text.removePrefix())
                        }
                    }
                }
        return namesOfVariables
    }

    /**
     * Warns that variable have a confusing name
     */
    private fun warnConfusingName(variableName: ASTNode) {
        val warnText = when (variableName.text) {
            "O", "D" -> "better name is: obj, dgt"
            "I", "l" -> "better name is: it, ln, line"
            "Z" -> "better name is: n1, n2"
            "S" -> "better name is: xs, str"
            "e" -> "better name is: ex, elm"
            "B" -> "better name is: bt, nxt"
            "h", "n" -> "better name is: nr, head, height"
            "m", "rn" -> "better name is: mbr, item"
            else -> ""

        }
        CONFUSING_IDENTIFIER_NAMING.warn(configRules, emitWarn, false, warnText, variableName.startOffset, variableName)
    }

    /**
     * Getting identifiers (aka variable names) from parent nodes like PROPERTY.
     * Several things to take into account here:
     *     * need to handle DESTRUCTURING_DECLARATION correctly, as it does not have IDENTIFIER leaf.
     *     * function type can have VALUE_PARAMETERs without name
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun extractVariableIdentifiers(node: ASTNode): List<ASTNode> {
        val destructingDeclaration = node.getFirstChildWithType(DESTRUCTURING_DECLARATION)
        val result = if (destructingDeclaration != null) {
            destructingDeclaration.getAllChildrenWithType(DESTRUCTURING_DECLARATION_ENTRY)
                    .map { it.getIdentifierName()!! }
        } else if (node.parents().count() > 1 && node.treeParent.elementType == VALUE_PARAMETER_LIST &&
                node.treeParent.treeParent.elementType == FUNCTION_TYPE) {
            listOfNotNull(node.getIdentifierName())
        } else {
            listOf(node.getIdentifierName()!!)
        }

        // no need to do checks if variables are in a special list with exceptions
        return result.filterNot { ONE_CHAR_IDENTIFIERS.contains(it.text) }
    }

    /**
     * basic check for class naming (PascalCase)
     * and checks for generic type declared for this class
     */
    private fun checkCLassNamings(node: ASTNode): List<ASTNode> {
        val genericType: ASTNode? = node.getTypeParameterList()
        if (genericType != null && !validGenericTypeName(genericType)) {
            GENERIC_NAME.warnAndFix(configRules, emitWarn, isFixMode, genericType.text, genericType.startOffset, genericType) {
                // FixMe: should fix generic name here
            }
        }

        val className: ASTNode = node.getIdentifierName() ?: return listOf()
        if (!(className.text.isPascalCase())) {
            CLASS_NAME_INCORRECT.warnAndFix(configRules, emitWarn, isFixMode, className.text, className.startOffset, className) {
                (className as LeafPsiElement).replaceWithText(className.text.toPascalCase())
            }
        }

        checkExceptionSuffix(node)
        return listOf(className)
    }

    /**
     * all exceptions should have Exception suffix
     *
     */
    private fun checkExceptionSuffix(node: ASTNode) {

        fun hasExceptionSuffix(text: String) = text.toLowerCase().endsWith("exception")

        val classNameNode = node.getIdentifierName() ?: return
        // getting super class name
        val superClassName: String? = node
                .getFirstChildWithType(ElementType.SUPER_TYPE_LIST)
                ?.findLeafWithSpecificType(TYPE_REFERENCE)
                ?.text

        if (superClassName != null && hasExceptionSuffix(superClassName) && !hasExceptionSuffix(classNameNode.text)) {
            EXCEPTION_SUFFIX.warnAndFix(configRules, emitWarn, isFixMode, classNameNode.text, classNameNode.startOffset, classNameNode) {
                // FixMe: need to add tests for this
                (classNameNode as LeafPsiElement).replaceWithText(classNameNode.text + "Exception")
            }
        }
    }

    /**
     * basic check for object naming of code blocks (PascalCase)
     * fix: fixing object name to PascalCase
     */
    private fun checkObjectNaming(node: ASTNode): List<ASTNode> {
        // if this object is companion object or anonymous object - it does not have any name
        val objectName: ASTNode = node.getIdentifierName() ?: return listOf()
        if (!objectName.text.isPascalCase()) {
            OBJECT_NAME_INCORRECT.warnAndFix(configRules, emitWarn, isFixMode, objectName.text, objectName.startOffset, objectName) {
                (objectName as LeafPsiElement).replaceWithText(objectName.text.toPascalCase())
            }
        }
        return listOf(objectName)
    }

    /**
     * check that Enum values match correct case and style
     * node has ENUM_ENTRY type
     * to check all variables will need to check all IDENTIFIERS in ENUM_ENTRY
     */
    private fun checkEnumValues(node: ASTNode): List<ASTNode> {
        val enumValues: List<ASTNode> = node.getChildren(null).filter { it.elementType == ElementType.IDENTIFIER }
        enumValues.forEach { value ->
            val configuration = IdentifierNamingConfiguration(configRules.getRuleConfig(ENUM_VALUE)?.configuration
                    ?: mapOf())
            val validator = when (configuration.enumStyle) {
                Style.PASCAL_CASE -> String::isPascalCase
                Style.SNAKE_CASE -> String::isUpperSnakeCase
            }
            val autofix = when (configuration.enumStyle) {
                Style.PASCAL_CASE -> String::toPascalCase
                Style.SNAKE_CASE -> String::toUpperSnakeCase
            }
            if (!validator(value.text)) {
                ENUM_VALUE.warnAndFix(configRules, emitWarn, isFixMode, value.text, value.startOffset, value) {
                    // FixMe: add tests for this
                    (value as LeafPsiElement).replaceWithText(autofix(value.text))
                }
            }

            if (CONFUSING_IDENTIFIER_NAMES.contains(value.text)) {
                warnConfusingName(value)
            }
        }

        return enumValues
    }

    /**
     * Check function name:
     * 1) function names should be in camel case
     * 2) methods that return boolean value should have "is"/"has" prefix
     * 3) FixMe: The function name is usually a verb or verb phrase (need to add check/fix for it)
     * 4) backticks are prohibited in the naming of non-test methods
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun checkFunctionName(node: ASTNode): List<ASTNode> {
        val functionName = node.getIdentifierName()!!

        // basic check for camel case
        if (!functionName.text.isLowerCamelCase()) {
            FUNCTION_NAME_INCORRECT_CASE.warnAndFix(configRules, emitWarn, isFixMode, functionName.text, functionName.startOffset, functionName) {
                // FixMe: add tests for this
                (functionName as LeafPsiElement).replaceWithText(functionName.text.toLowerCamelCase())
            }
        }

        // check for methods that return Boolean
        val functionReturnType = node.findChildAfter(VALUE_PARAMETER_LIST, TYPE_REFERENCE)?.text

        // We don't need to ask subclasses to rename superclass methods
        if (!node.isOverriden()) {
            // if function has Boolean return type in 99% of cases it is much better to name it with isXXX or hasXXX prefix
            if (functionReturnType != null && functionReturnType == PrimitiveType.BOOLEAN.typeName.asString()) {
                if (BOOLEAN_METHOD_PREFIXES.none { functionName.text.startsWith(it) }) {
                    FUNCTION_BOOLEAN_PREFIX.warnAndFix(configRules, emitWarn, isFixMode, functionName.text, functionName.startOffset, functionName) {
                        // FixMe: add agressive autofix for this
                    }
                }
            }
        }

        return listOf(functionName)
    }


    /**
     * check that generic name has single capital letter, can be followed by a number
     * this method will check it for both generic classes and generic methods
     */
    private fun validGenericTypeName(generic: ASTNode): Boolean {
        return generic.getChildren(TokenSet.create(TYPE_PARAMETER)).all{
            val typeText = it.getIdentifierName()?.text ?: return false
            // first letter should always be a capital and other letters - are digits
            typeText[0] in 'A'..'Z' &&  (typeText.length == 1 || typeText.substring(1).isDigits())
        }
    }

    /**
     * identifier name length should not be longer than 64 symbols and shorter than 2 symbols
     */
    private fun checkIdentifierLength(nodes: List<ASTNode>,
                                      isVariable: Boolean) {
        nodes.forEach {
            if (it.text != "_" && !(it.checkLength(MIN_IDENTIFIER_LENGTH..MAX_IDENTIFIER_LENGTH) ||
                            ONE_CHAR_IDENTIFIERS.contains(it.text) && isVariable || validCatchIdentifier(it)

                            )) {
                IDENTIFIER_LENGTH.warn(configRules, emitWarn, isFixMode, it.text, it.startOffset, it)
            }
        }
    }

    /**
     * exception case for identifiers used in catch block:
     * catch (e: Exception) {}
     */
    private fun validCatchIdentifier(node: ASTNode): Boolean {
        val parentValueParamList = node.findParentNodeWithSpecificType(VALUE_PARAMETER_LIST)
        val prevCatchKeyWord = parentValueParamList?.prevCodeSibling()?.elementType == CATCH_KEYWORD
        return node.text == "e" && node.findParentNodeWithSpecificType(CATCH) != null && prevCatchKeyWord
    }

    class IdentifierNamingConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        private val Style.isEnumStyle: Boolean
            get() = listOf(Style.PASCAL_CASE, Style.SNAKE_CASE).contains(this)

        val enumStyle = config["enumStyle"]?.let { styleString ->
            val style = Style.values().firstOrNull {
                it.name == styleString.toUpperSnakeCase()
            }
            if (style == null || !style.isEnumStyle) {
                error("$styleString is unsupported for enum style")
            }
            style
        } ?: Style.SNAKE_CASE
    }
}
