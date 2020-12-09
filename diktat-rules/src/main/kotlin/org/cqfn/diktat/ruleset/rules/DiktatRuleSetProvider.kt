package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.DIKTAT_COMMON
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.calculations.AccurateCalculationsRule
import org.cqfn.diktat.ruleset.rules.classes.AbstractClassesRule
import org.cqfn.diktat.ruleset.rules.classes.CompactInitialization
import org.cqfn.diktat.ruleset.rules.classes.DataClassesRule
import org.cqfn.diktat.ruleset.rules.classes.SingleConstructorRule
import org.cqfn.diktat.ruleset.rules.classes.SingleInitRule
import org.cqfn.diktat.ruleset.rules.comments.CommentsRule
import org.cqfn.diktat.ruleset.rules.comments.HeaderCommentRule
import org.cqfn.diktat.ruleset.rules.files.BlankLinesRule
import org.cqfn.diktat.ruleset.rules.files.FileSize
import org.cqfn.diktat.ruleset.rules.files.FileStructureRule
import org.cqfn.diktat.ruleset.rules.files.IndentationRule
import org.cqfn.diktat.ruleset.rules.files.NewlinesRule
import org.cqfn.diktat.ruleset.rules.files.WhiteSpaceRule
import org.cqfn.diktat.ruleset.rules.identifiers.LocalVariablesRule
import org.cqfn.diktat.ruleset.rules.kdoc.CommentsFormatting
import org.cqfn.diktat.ruleset.rules.kdoc.KdocComments
import org.cqfn.diktat.ruleset.rules.kdoc.KdocFormatting
import org.cqfn.diktat.ruleset.rules.kdoc.KdocMethods

import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.cqfn.diktat.ruleset.rules.classes.StatelessClassesRule
import org.jetbrains.kotlin.org.jline.utils.Levenshtein
import org.slf4j.LoggerFactory

import java.io.File

/**
 * this constant will be used everywhere in the code to mark usage of Diktat ruleset
 */
const val DIKTAT_RULE_SET_ID = "diktat-ruleset"
const val DIKTAT_ANALYSIS_CONF = "diktat-analysis.yml"

/**
 * [RuleSetProvider] that provides diKTat ruleset.
 * By default it is expected to have diktat-analysis.yml configuration in the root folder where 'ktlint' is run
 * otherwise it will use default configuration where some rules are disabled
 *
 * @param diktatConfigFile - configuration file where all configurations for inspections and rules are stored
 */
class DiktatRuleSetProvider(private var diktatConfigFile: String = DIKTAT_ANALYSIS_CONF) : RuleSetProvider {
    @Suppress("LongMethod", "TOO_LONG_FUNCTION")
    override fun get(): RuleSet {
        log.debug("Will run $DIKTAT_RULE_SET_ID with $diktatConfigFile" +
                " (it can be placed to the run directory or the default file from resources will be used)")
        val diktatExecutionPath = File(diktatConfigFile)
        if (!diktatExecutionPath.exists()) {
            // for some aggregators of static analyzers we need to provide configuration for cli
            // in this case diktat would take the configuration from the directory where jar file is stored
            val ruleSetProviderPath =
                    DiktatRuleSetProvider::class
                        .java
                        .protectionDomain
                        .codeSource
                        .location
                        .toURI()

            val configPathWithFileName = File(ruleSetProviderPath).absolutePath

            val indexOfName = configPathWithFileName.lastIndexOf(File.separator)
            val configPath = if (indexOfName > -1) configPathWithFileName.substring(0, indexOfName) else configPathWithFileName
            diktatConfigFile = "$configPath${File.separator}$diktatConfigFile"

            if (!File(diktatConfigFile).exists()) {
                log.warn("Configuration file not found in directory where diktat is run (${diktatExecutionPath.absolutePath}) " +
                        "or in the directory where diktat.jar is stored ($diktatConfigFile), " +
                        "the default file included in jar will be used. " +
                        "Some configuration options will be disabled or substituted with defaults. " +
                        "Custom configuration file should be placed in diktat working directory if run from CLI " +
                        "or provided as configuration options in plugins."
                )
            }
        }

        val configRules = RulesConfigReader(javaClass.classLoader)
            .readResource(diktatConfigFile)
            ?.onEach(::validate)
            ?: emptyList()
        // Note: the order of rules is important in autocorrect mode. For example, all rules that add new code should be invoked before rules that fix formatting.
        // We don't have a way to enforce a specific order, so we should just be careful when adding new rules to this list and, when possible,
        // cover new rules in smoke test as well. If a rule needs to be at a specific position in a list, please add comment explaining it (like for NewlinesRule).
        val rules = listOf(
            // comments & documentation
            ::CommentsRule,
            ::SingleConstructorRule,  // this rule can add properties to a primary constructor, so should be before KdocComments
            ::KdocComments,
            ::KdocMethods,
            ::KdocFormatting,
            ::CommentsFormatting,
            // naming
            ::FileNaming,
            ::PackageNaming,
            ::IdentifierNaming,
            // code structure
            ::UselessSupertype,
            ::ClassLikeStructuresOrderRule,
            ::WhenMustHaveElseRule,
            ::BracesInConditionalsAndLoopsRule,
            ::BlockStructureBraces,
            ::EmptyBlock,
            ::AvoidEmptyPrimaryConstructor,
            ::EnumsSeparated,
            ::SingleLineStatementsRule,
            ::MultipleModifiersSequence,
            ::TrivialPropertyAccessors,
            ::CustomGetterSetterRule,
            ::CompactInitialization,
            // other rules
            ::StatelessClassesRule,
            ::ImplicitBackingPropertyRule,
            ::StringTemplateFormatRule,
            ::DataClassesRule,
            ::LocalVariablesRule,
            ::SmartCastRule,
            ::AvoidUtilityClass,
            ::PropertyAccessorFields,
            ::AbstractClassesRule,
            ::SingleInitRule,
            ::VariableGenericTypeDeclarationRule,
            ::LongNumericalValuesSeparatedRule,
            ::NestedFunctionBlock,
            ::AnnotationNewLineRule,
            ::SortRule,
            ::StringConcatenationRule,
            ::AccurateCalculationsRule,
            ::LineLength,
            ::TypeAliasRule,
            ::OverloadingArgumentsFunction,
            ::FunctionLength,
            ::LambdaParameterOrder,
            ::FunctionArgumentsSize,
            ::BlankLinesRule,
            ::FileSize,
            ::NullableTypeRule,
            ::NullChecksRule,
            ::ImmutableValNoVarRule,
            ::AvoidNestedFunctionsRule,
            ::ExtensionFunctionsSameNameRule,
            // formatting: moving blocks, adding line breaks, indentations etc.
            ::ConsecutiveSpacesRule,
            ::HeaderCommentRule,
            ::FileStructureRule,  // this rule should be right before indentation because it should operate on already valid code
            ::NewlinesRule,  // newlines need to be inserted right before fixing indentation
            ::WhiteSpaceRule,  // this rule should be after other rules that can cause wrong spacing
            ::IndentationRule  // indentation rule should be the last because it fixes formatting after all the changes done by previous rules
        )
            .map {
                it.invoke(configRules)
            }
            .toTypedArray()
        return RuleSet(
            DIKTAT_RULE_SET_ID,
            *rules
        )
    }

    private fun validate(config: RulesConfig) =
            require(config.name == DIKTAT_COMMON || config.name in Warnings.names) {
                val closestMatch = Warnings.names.minBy { Levenshtein.distance(it, config.name) }
                "Warning name <${config.name}> in configuration file is invalid, did you mean <$closestMatch>?"
            }

    companion object {
        private val log = LoggerFactory.getLogger(DiktatRuleSetProvider::class.java)
    }
}
