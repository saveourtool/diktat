@file:Suppress("FILE_WILDCARD_IMPORTS")

package com.saveourtool.diktat.ruleset.rules.chapter1

import com.saveourtool.diktat.common.config.rules.RuleConfiguration
import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.common.config.rules.getRuleConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.BACKTICKS_PROHIBITED
import com.saveourtool.diktat.ruleset.constants.Warnings.CLASS_NAME_INCORRECT
import com.saveourtool.diktat.ruleset.constants.Warnings.CONFUSING_IDENTIFIER_NAMING
import com.saveourtool.diktat.ruleset.constants.Warnings.CONSTANT_UPPERCASE
import com.saveourtool.diktat.ruleset.constants.Warnings.ENUM_VALUE
import com.saveourtool.diktat.ruleset.constants.Warnings.EXCEPTION_SUFFIX
import com.saveourtool.diktat.ruleset.constants.Warnings.FUNCTION_BOOLEAN_PREFIX
import com.saveourtool.diktat.ruleset.constants.Warnings.FUNCTION_NAME_INCORRECT_CASE
import com.saveourtool.diktat.ruleset.constants.Warnings.GENERIC_NAME
import com.saveourtool.diktat.ruleset.constants.Warnings.IDENTIFIER_LENGTH
import com.saveourtool.diktat.ruleset.constants.Warnings.OBJECT_NAME_INCORRECT
import com.saveourtool.diktat.ruleset.constants.Warnings.TYPEALIAS_NAME_INCORRECT_CASE
import com.saveourtool.diktat.ruleset.constants.Warnings.VARIABLE_HAS_PREFIX
import com.saveourtool.diktat.ruleset.constants.Warnings.VARIABLE_NAME_INCORRECT
import com.saveourtool.diktat.ruleset.constants.Warnings.VARIABLE_NAME_INCORRECT_FORMAT
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.*
import com.saveourtool.diktat.ruleset.utils.search.findAllVariablesWithUsages

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.KtNodeTypes.CATCH
import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.KtNodeTypes.DESTRUCTURING_DECLARATION
import org.jetbrains.kotlin.KtNodeTypes.DESTRUCTURING_DECLARATION_ENTRY
import org.jetbrains.kotlin.KtNodeTypes.FUNCTION_TYPE
import org.jetbrains.kotlin.KtNodeTypes.OBJECT_DECLARATION
import org.jetbrains.kotlin.KtNodeTypes.PROPERTY
import org.jetbrains.kotlin.KtNodeTypes.REFERENCE_EXPRESSION
import org.jetbrains.kotlin.KtNodeTypes.TYPE_PARAMETER
import org.jetbrains.kotlin.KtNodeTypes.TYPE_REFERENCE
import org.jetbrains.kotlin.KtNodeTypes.VALUE_PARAMETER_LIST
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.KDOC
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.lexer.KtTokens.CATCH_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtPrimaryConstructor
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.psiUtil.isPrivate
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

import java.util.Locale

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
@Suppress("ForbiddenComment", "MISSING_KDOC_CLASS_ELEMENTS")
class IdentifierNaming(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(BACKTICKS_PROHIBITED, VARIABLE_NAME_INCORRECT, VARIABLE_NAME_INCORRECT_FORMAT, CONSTANT_UPPERCASE,
        VARIABLE_HAS_PREFIX, CONFUSING_IDENTIFIER_NAMING, GENERIC_NAME, CLASS_NAME_INCORRECT,
        ENUM_VALUE, EXCEPTION_SUFFIX, FUNCTION_BOOLEAN_PREFIX, FUNCTION_NAME_INCORRECT_CASE,
        IDENTIFIER_LENGTH, OBJECT_NAME_INCORRECT, TYPEALIAS_NAME_INCORRECT_CASE)
) {
    private val allMethodPrefixes by lazy {
        if (configuration.allowedBooleanPrefixes.isEmpty()) {
            booleanMethodPrefixes
        } else {
            booleanMethodPrefixes + configuration.allowedBooleanPrefixes.filter { it.isNotEmpty() }
        }
    }
    val configuration by lazy {
        BooleanFunctionsConfiguration(
            this.configRules.getRuleConfig(FUNCTION_BOOLEAN_PREFIX)?.configuration ?: emptyMap()
        )
    }

    override fun logic(
        node: ASTNode
    ) {
        // backticks are prohibited for identifier declarations everywhere except test methods that are marked with @Test annotation
        if (isIdentifierWithBackticks(node)) {
            return
        }

        // isVariable is used as a workaround to check corner case with variables that have length == 1
        val (identifierNodes, isVariable) = when (node.elementType) {
            // covers interface, class, enum class and annotation class names
            KtNodeTypes.CLASS -> Pair(checkClassNamings(node), false)
            // covers "object" code blocks
            KtNodeTypes.OBJECT_DECLARATION -> Pair(checkObjectNaming(node), false)
            // covers variables (val/var), constants (const val) and parameters for lambdas
            KtNodeTypes.PROPERTY, KtNodeTypes.VALUE_PARAMETER -> Pair(checkVariableName(node), true)
            // covers case of enum values
            KtNodeTypes.ENUM_ENTRY -> Pair(checkEnumValues(node), false)
            // covers global functions, extensions and class methods
            KtNodeTypes.FUN -> Pair(checkFunctionName(node), false)
            // covers case of typeAlias values
            KtNodeTypes.TYPEALIAS -> Pair(checkTypeAliases(node), false)
            else -> Pair(null, false)
        }

        identifierNodes?.let {
            checkIdentifierLength(it, isVariable)
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
                val isTestFun = node.elementType == KtNodeTypes.FUN && node.hasTestAnnotation()
                if (!isTestFun) {
                    BACKTICKS_PROHIBITED.warn(configRules, emitWarn, identifierText, identifier.startOffset, identifier)
                }
                return true
            }
        }

        return false
    }

    /**
     * all checks for case and naming for vals/vars/constants
     */
    @Suppress(
        "SAY_NO_TO_VAR",
        "TOO_LONG_FUNCTION",
        "LongMethod",
        "ComplexMethod",
        "UnsafeCallOnNullableType",
    )

    private fun checkVariableName(node: ASTNode): List<ASTNode> {
        val configuration = ConstantUpperCaseConfiguration(
            configRules.getRuleConfig(CONSTANT_UPPERCASE)?.configuration
                ?: emptyMap())

        val exceptionNames = configuration.exceptionConstNames

        // special case for Destructuring declarations that can be treated as parameters in lambda:
        var namesOfVariables = extractVariableIdentifiers(node)

        // Only local private properties will be autofix in order not to break code if there are usages in other files.
        // Destructuring declarations are only allowed for local variables/values, so we don't need to calculate `isFix` for every node in `namesOfVariables`
        val isPublicOrNonLocalProperty = if (node.elementType == KtNodeTypes.PROPERTY) (node.psi as KtProperty).run { !isLocal && !isPrivate() } else false
        val isNonPrivatePrimaryConstructorParameter = (node.psi as? KtParameter)?.run {
            hasValOrVar() && getParentOfType<KtPrimaryConstructor>(true)?.valueParameters?.contains(this) == true && !isPrivate()
        } ?: false
        val shouldBeAutoCorrected = !(isPublicOrNonLocalProperty || isNonPrivatePrimaryConstructorParameter)
        namesOfVariables
            .forEach { variableName ->
                // variable should not contain only one letter in it's name. This is a bad example: b512
                // but no need to raise a warning here if length of a variable. In this case we will raise IDENTIFIER_LENGTH
                if (variableName.text.containsOneLetterOrZero() && variableName.text.length > 1) {
                    VARIABLE_NAME_INCORRECT.warn(configRules, emitWarn, variableName.text, variableName.startOffset, node)
                }
                // check if identifier of a property has a confusing name
                if (confusingIdentifierNames.contains(variableName.text) && !isValidCatchIdentifier(variableName) &&
                        node.elementType == KtNodeTypes.PROPERTY
                ) {
                    warnConfusingName(variableName)
                }
                // check for constant variables - check for val from companion object or on global file level
                // it should be in UPPER_CASE, no need to raise this warning if it is one-letter variable
                if (node.isConstant()) {
                    if (!exceptionNames.contains(variableName.text) && !variableName.text.isUpperSnakeCase() && variableName.text.length > 1) {
                        CONSTANT_UPPERCASE.warnOnlyOrWarnAndFix(
                            configRules = configRules,
                            emit = emitWarn,
                            freeText = variableName.text,
                            offset = variableName.startOffset,
                            node = node,
                            shouldBeAutoCorrected = shouldBeAutoCorrected,
                            isFixMode = isFixMode,
                        ) {
                            (variableName as LeafPsiElement).rawReplaceWithText(variableName.text.toDeterministic { toUpperSnakeCase() })
                        }
                    }
                } else if (variableName.text != "_" && !variableName.text.isLowerCamelCase() &&
                        // variable name should be in camel case. The only exception is a list of industry standard variables like i, j, k.
                        !isPairPropertyBackingField(null, node.psi as? KtProperty)
                ) {
                    VARIABLE_NAME_INCORRECT_FORMAT.warnOnlyOrWarnAndFix(
                        configRules = configRules,
                        emit = emitWarn,
                        freeText = variableName.text,
                        offset = variableName.startOffset,
                        node = node,
                        shouldBeAutoCorrected = shouldBeAutoCorrected,
                        isFixMode = isFixMode,
                    ) {
                        // FixMe: cover fixes with tests
                        val correctVariableName = variableName.text.toDeterministic { toLowerCamelCase() }
                        variableName
                            .parent { it.elementType == KtFileElementType.INSTANCE }
                            ?.findAllVariablesWithUsages { it.name == variableName.text }
                            ?.flatMap { it.value.toList() }
                            ?.forEach { (it.node.firstChildNode as LeafPsiElement).rawReplaceWithText(correctVariableName) }
                        if (variableName.treeParent.psi.run {
                            (this is KtProperty && isMember) ||
                                    (this is KtParameter && getParentOfType<KtPrimaryConstructor>(true)?.valueParameters?.contains(this) == true)
                        }) {
                            // For class members also check `@property` KDoc tag.
                            // If we are here, then `variableName` is definitely a node from a class or an object.
                            (variableName.parent(CLASS) ?: variableName.parent(OBJECT_DECLARATION))?.findChildByType(KDOC)?.kDocTags()
                                ?.firstOrNull {
                                    it.knownTag == KDocKnownTag.PROPERTY && it.getSubjectName() == variableName.text
                                }
                                ?.run {
                                    (getSubjectLink()!!.node.findAllDescendantsWithSpecificType(IDENTIFIER).single() as LeafPsiElement).rawReplaceWithText(correctVariableName)
                                }
                        }
                        (variableName as LeafPsiElement).rawReplaceWithText(correctVariableName)
                    }
                }
            }

        // need to get new node in case we have already converted the case before (and replaced the child node)
        // we need to recalculate it twice, because nodes could have been changed by "rawReplaceWithText" function
        namesOfVariables = extractVariableIdentifiers(node)
        namesOfVariables
            .forEach { variableName ->
                // generally, variables with prefixes are not allowed (like mVariable, xCode, iValue)
                if (variableName.text.hasPrefix()) {
                    VARIABLE_HAS_PREFIX.warnAndFix(configRules, emitWarn, isFixMode, variableName.text, variableName.startOffset, node) {
                        (variableName as LeafPsiElement).rawReplaceWithText(variableName.text.removePrefix())
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
        CONFUSING_IDENTIFIER_NAMING.warn(configRules, emitWarn, warnText, variableName.startOffset, variableName)
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
                node.treeParent.treeParent.elementType == FUNCTION_TYPE
        ) {
            listOfNotNull(node.getIdentifierName())
        } else {
            listOf(node.getIdentifierName()!!)
        }

        // no need to do checks if variables are in a special list with exceptions
        return result.filterNot { oneCharIdentifiers.contains(it.text) }
    }

    /**
     * basic check for class naming (PascalCase)
     * and checks for generic type declared for this class
     */
    private fun checkClassNamings(node: ASTNode): List<ASTNode> {
        val genericType: ASTNode? = node.getTypeParameterList()
        if (genericType != null && !validGenericTypeName(genericType)) {
            // FixMe: should fix generic name here
            GENERIC_NAME.warn(configRules, emitWarn, genericType.text, genericType.startOffset, genericType)
        }

        val className: ASTNode = node.getIdentifierName() ?: return emptyList()
        if (!(className.text.isPascalCase())) {
            CLASS_NAME_INCORRECT.warnAndFix(configRules, emitWarn, isFixMode, className.text, className.startOffset, className) {
                (className as LeafPsiElement).rawReplaceWithText(className.text.toDeterministic { toPascalCase() })
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
        val classNameNode = node.getIdentifierName() ?: return
        // getting super class name
        val superClassName: String? = node
            .getFirstChildWithType(KtNodeTypes.SUPER_TYPE_LIST)
            ?.findLeafWithSpecificType(TYPE_REFERENCE)
            ?.text

        if (superClassName != null && hasExceptionSuffix(superClassName) && !hasExceptionSuffix(classNameNode.text)) {
            EXCEPTION_SUFFIX.warnAndFix(configRules, emitWarn, isFixMode, classNameNode.text, classNameNode.startOffset, classNameNode) {
                // FixMe: need to add tests for this
                (classNameNode as LeafPsiElement).rawReplaceWithText(classNameNode.text + "Exception")
            }
        }
    }

    private fun hasExceptionSuffix(text: String) = text.lowercase(Locale.getDefault()).endsWith("exception")

    /**
     * basic check for object naming of code blocks (PascalCase)
     * fix: fixing object name to PascalCase
     */
    private fun checkObjectNaming(node: ASTNode): List<ASTNode> {
        // if this object is companion object or anonymous object - it does not have any name
        val objectName: ASTNode = node.getIdentifierName() ?: return emptyList()
        if (!objectName.text.isPascalCase()) {
            OBJECT_NAME_INCORRECT.warnAndFix(configRules, emitWarn, isFixMode, objectName.text, objectName.startOffset, objectName) {
                (objectName as LeafPsiElement).rawReplaceWithText(objectName.text.toDeterministic { toPascalCase() })
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
        val enumValues: List<ASTNode> = node.getChildren(null).filter { it.elementType == KtTokens.IDENTIFIER }
        enumValues.forEach { value ->
            val configuration = IdentifierNamingConfiguration(
                configRules.getRuleConfig(ENUM_VALUE)?.configuration
                    ?: emptyMap()
            )
            val validator = when (configuration.enumStyle) {
                Style.PASCAL_CASE -> String::isPascalCase
                Style.SNAKE_CASE -> String::isUpperSnakeCase
            }
            val autofix = when (configuration.enumStyle) {
                Style.PASCAL_CASE -> String::toPascalCase
                Style.SNAKE_CASE -> String::toUpperSnakeCase
            }
            if (!validator(value.text)) {
                ENUM_VALUE.warnAndFix(
                    configRules,
                    emitWarn,
                    isFixMode,
                    "${value.text} (should be in ${configuration.enumStyle.str})",
                    value.startOffset, value
                ) {
                    // FixMe: add tests for this
                    (value as LeafPsiElement).rawReplaceWithText(autofix(value.text))
                }
            }

            if (confusingIdentifierNames.contains(value.text)) {
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
    private fun checkFunctionName(node: ASTNode): List<ASTNode>? {
        val functionName = node.getIdentifierName() ?: return null

        // basic check for camel case
        if (!functionName.text.isLowerCamelCase()) {
            FUNCTION_NAME_INCORRECT_CASE.warnAndFix(configRules, emitWarn, isFixMode, functionName.text, functionName.startOffset, functionName) {
                // FixMe: add tests for this
                (functionName as LeafPsiElement).rawReplaceWithText(functionName.text.toDeterministic { toLowerCamelCase() })
            }
        }

        // We don't need to ask subclasses to rename superclass methods
        if (!node.isOverridden()) {
            // check for methods that return Boolean
            // if function has Boolean return type in 99% of cases it is much better to name it with isXXX or hasXXX prefix
            @Suppress("COLLAPSE_IF_STATEMENTS")

            if (node.hasBooleanReturnType() && !node.isOperatorFun() && allMethodPrefixes.none { functionName.text.startsWith(it) }) {
                // FixMe: add agressive autofix for this
                FUNCTION_BOOLEAN_PREFIX.warn(configRules, emitWarn, functionName.text, functionName.startOffset, functionName)
            }
        }

        return listOf(functionName)
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkTypeAliases(node: ASTNode): List<ASTNode> {
        val aliasName = node.getIdentifierName()!!

        if (!aliasName.text.isPascalCase()) {
            TYPEALIAS_NAME_INCORRECT_CASE.warnAndFix(configRules, emitWarn, isFixMode, aliasName.text, aliasName.startOffset, aliasName) {
                (aliasName as LeafPsiElement).rawReplaceWithText(aliasName.text.toDeterministic { toPascalCase() })
            }
        }
        return listOf(aliasName)
    }

    /**
     * check that generic name has single capital letter, can be followed by a number
     * this method will check it for both generic classes and generic methods
     */
    private fun validGenericTypeName(generic: ASTNode) = generic.getChildren(TokenSet.create(TYPE_PARAMETER)).all {
        val typeText = it.getIdentifierName()?.text ?: return false
        // first letter should always be a capital and other letters - are digits
        typeText[0] in 'A'..'Z' && (typeText.length == 1 || typeText.substring(1).isDigits())
    }

    /**
     * identifier name length should not be longer than 64 symbols and shorter than 2 symbols
     */
    private fun checkIdentifierLength(
        nodes: List<ASTNode>,
        isVariable: Boolean
    ) {
        nodes.forEach {
            val isValidOneCharVariable = oneCharIdentifiers.contains(it.text) && isVariable
            if (it.text != "_" && !it.isTextLengthInRange(MIN_IDENTIFIER_LENGTH..MAX_IDENTIFIER_LENGTH) &&
                    !isValidOneCharVariable && !isValidCatchIdentifier(it)
            ) {
                IDENTIFIER_LENGTH.warn(configRules, emitWarn, it.text, it.startOffset, it)
            }
        }
    }

    /**
     * exception case for identifiers used in catch block:
     * catch (e: Exception) {}
     */
    private fun isValidCatchIdentifier(node: ASTNode): Boolean {
        val parentValueParamList = node.findParentNodeWithSpecificType(VALUE_PARAMETER_LIST)
        val prevCatchKeyWord = parentValueParamList?.prevCodeSibling()?.elementType == CATCH_KEYWORD
        return node.text == "e" && node.findParentNodeWithSpecificType(CATCH) != null && prevCatchKeyWord
    }

    /**
     * [RuleConfiguration] for identifier naming
     */
    class IdentifierNamingConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        @Suppress("CUSTOM_GETTERS_SETTERS")
        private val Style.isEnumStyle: Boolean
            get() = listOf(Style.PASCAL_CASE, Style.SNAKE_CASE).contains(this)

        /**
         * In which style enum members should be named
         */
        val enumStyle = config["enumStyle"]?.let { styleString ->
            val style = Style.entries.firstOrNull {
                it.name == styleString.toUpperSnakeCase()
            }
            if (style == null || !style.isEnumStyle) {
                error("$styleString is unsupported for enum style")
            }
            style
        } ?: Style.SNAKE_CASE
    }

    class ConstantUpperCaseConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        val exceptionConstNames = config["exceptionConstNames"]?.split(',') ?: emptyList()
    }

    class BooleanFunctionsConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * A list of functions that return boolean and are allowed to use. Input is in a form "foo, bar".
         */
        val allowedBooleanPrefixes = config["allowedPrefixes"]?.split(",")?.map { it.trim() } ?: emptyList()
    }

    companion object {
        private const val MAX_DETERMINISTIC_RUNS = 5
        const val MAX_IDENTIFIER_LENGTH = 64
        const val MIN_IDENTIFIER_LENGTH = 2
        const val NAME_ID = "identifier-naming"

        // FixMe: this should be moved to properties
        val oneCharIdentifiers = setOf("i", "j", "k", "x", "y", "z")
        val booleanMethodPrefixes = setOf("has", "is", "are", "have", "should", "can")
        val confusingIdentifierNames = setOf("O", "D", "I", "l", "Z", "S", "e", "B", "h", "n", "m", "rn")

        private fun String.toDeterministic(function: String.() -> String): String = generateSequence(function(this), function)
            .runningFold(this to false) { (current, result), next ->
                require(!result) {
                    "Should return a value already"
                }
                next to (current == next)
            }
            .take(MAX_DETERMINISTIC_RUNS)
            .first { it.second }
            .first
    }
}
