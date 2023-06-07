package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.api.DiktatRuleConfig
import org.cqfn.diktat.api.DiktatRuleSet
import org.cqfn.diktat.api.DiktatRuleSetFactory
import org.cqfn.diktat.ruleset.rules.chapter1.FileNaming
import org.cqfn.diktat.ruleset.rules.chapter1.IdentifierNaming
import org.cqfn.diktat.ruleset.rules.chapter1.PackageNaming
import org.cqfn.diktat.ruleset.rules.chapter2.comments.CommentsRule
import org.cqfn.diktat.ruleset.rules.chapter2.comments.HeaderCommentRule
import org.cqfn.diktat.ruleset.rules.chapter2.kdoc.CommentsFormatting
import org.cqfn.diktat.ruleset.rules.chapter2.kdoc.KdocComments
import org.cqfn.diktat.ruleset.rules.chapter2.kdoc.KdocFormatting
import org.cqfn.diktat.ruleset.rules.chapter2.kdoc.KdocMethods
import org.cqfn.diktat.ruleset.rules.chapter3.AnnotationNewLineRule
import org.cqfn.diktat.ruleset.rules.chapter3.BlockStructureBraces
import org.cqfn.diktat.ruleset.rules.chapter3.BooleanExpressionsRule
import org.cqfn.diktat.ruleset.rules.chapter3.BracesInConditionalsAndLoopsRule
import org.cqfn.diktat.ruleset.rules.chapter3.ClassLikeStructuresOrderRule
import org.cqfn.diktat.ruleset.rules.chapter3.CollapseIfStatementsRule
import org.cqfn.diktat.ruleset.rules.chapter3.ConsecutiveSpacesRule
import org.cqfn.diktat.ruleset.rules.chapter3.DebugPrintRule
import org.cqfn.diktat.ruleset.rules.chapter3.EmptyBlock
import org.cqfn.diktat.ruleset.rules.chapter3.EnumsSeparated
import org.cqfn.diktat.ruleset.rules.chapter3.LineLength
import org.cqfn.diktat.ruleset.rules.chapter3.LongNumericalValuesSeparatedRule
import org.cqfn.diktat.ruleset.rules.chapter3.MagicNumberRule
import org.cqfn.diktat.ruleset.rules.chapter3.MultipleModifiersSequence
import org.cqfn.diktat.ruleset.rules.chapter3.NullableTypeRule
import org.cqfn.diktat.ruleset.rules.chapter3.RangeConventionalRule
import org.cqfn.diktat.ruleset.rules.chapter3.SingleLineStatementsRule
import org.cqfn.diktat.ruleset.rules.chapter3.SortRule
import org.cqfn.diktat.ruleset.rules.chapter3.StringConcatenationRule
import org.cqfn.diktat.ruleset.rules.chapter3.StringTemplateFormatRule
import org.cqfn.diktat.ruleset.rules.chapter3.TrailingCommaRule
import org.cqfn.diktat.ruleset.rules.chapter3.WhenMustHaveElseRule
import org.cqfn.diktat.ruleset.rules.chapter3.files.BlankLinesRule
import org.cqfn.diktat.ruleset.rules.chapter3.files.FileSize
import org.cqfn.diktat.ruleset.rules.chapter3.files.FileStructureRule
import org.cqfn.diktat.ruleset.rules.chapter3.files.IndentationRule
import org.cqfn.diktat.ruleset.rules.chapter3.files.NewlinesRule
import org.cqfn.diktat.ruleset.rules.chapter3.files.TopLevelOrderRule
import org.cqfn.diktat.ruleset.rules.chapter3.files.WhiteSpaceRule
import org.cqfn.diktat.ruleset.rules.chapter3.identifiers.LocalVariablesRule
import org.cqfn.diktat.ruleset.rules.chapter4.ImmutableValNoVarRule
import org.cqfn.diktat.ruleset.rules.chapter4.NullChecksRule
import org.cqfn.diktat.ruleset.rules.chapter4.SmartCastRule
import org.cqfn.diktat.ruleset.rules.chapter4.TypeAliasRule
import org.cqfn.diktat.ruleset.rules.chapter4.VariableGenericTypeDeclarationRule
import org.cqfn.diktat.ruleset.rules.chapter4.calculations.AccurateCalculationsRule
import org.cqfn.diktat.ruleset.rules.chapter5.AsyncAndSyncRule
import org.cqfn.diktat.ruleset.rules.chapter5.AvoidNestedFunctionsRule
import org.cqfn.diktat.ruleset.rules.chapter5.CheckInverseMethodRule
import org.cqfn.diktat.ruleset.rules.chapter5.CustomLabel
import org.cqfn.diktat.ruleset.rules.chapter5.FunctionArgumentsSize
import org.cqfn.diktat.ruleset.rules.chapter5.FunctionLength
import org.cqfn.diktat.ruleset.rules.chapter5.LambdaLengthRule
import org.cqfn.diktat.ruleset.rules.chapter5.LambdaParameterOrder
import org.cqfn.diktat.ruleset.rules.chapter5.NestedFunctionBlock
import org.cqfn.diktat.ruleset.rules.chapter5.OverloadingArgumentsFunction
import org.cqfn.diktat.ruleset.rules.chapter5.ParameterNameInOuterLambdaRule
import org.cqfn.diktat.ruleset.rules.chapter6.AvoidEmptyPrimaryConstructor
import org.cqfn.diktat.ruleset.rules.chapter6.AvoidUtilityClass
import org.cqfn.diktat.ruleset.rules.chapter6.CustomGetterSetterRule
import org.cqfn.diktat.ruleset.rules.chapter6.ExtensionFunctionsInFileRule
import org.cqfn.diktat.ruleset.rules.chapter6.ExtensionFunctionsSameNameRule
import org.cqfn.diktat.ruleset.rules.chapter6.ImplicitBackingPropertyRule
import org.cqfn.diktat.ruleset.rules.chapter6.PropertyAccessorFields
import org.cqfn.diktat.ruleset.rules.chapter6.RunInScript
import org.cqfn.diktat.ruleset.rules.chapter6.TrivialPropertyAccessors
import org.cqfn.diktat.ruleset.rules.chapter6.UseLastIndex
import org.cqfn.diktat.ruleset.rules.chapter6.UselessSupertype
import org.cqfn.diktat.ruleset.rules.chapter6.classes.AbstractClassesRule
import org.cqfn.diktat.ruleset.rules.chapter6.classes.CompactInitialization
import org.cqfn.diktat.ruleset.rules.chapter6.classes.DataClassesRule
import org.cqfn.diktat.ruleset.rules.chapter6.classes.InlineClassesRule
import org.cqfn.diktat.ruleset.rules.chapter6.classes.SingleConstructorRule
import org.cqfn.diktat.ruleset.rules.chapter6.classes.SingleInitRule
import org.cqfn.diktat.ruleset.rules.chapter6.classes.StatelessClassesRule

/**
 * _KtLint_-agnostic factory which creates a [DiktatRuleSet].
 *
 * By default, it is expected to have `diktat-analysis.yml` configuration in the root folder where 'ktlint' is run
 * otherwise it will use default configuration where some rules are disabled.
 */
class DiktatRuleSetFactoryImpl : DiktatRuleSetFactory {
    /**
     * This method is going to be called once for each file (which means if any
     * of the rules have state or are not thread-safe - a new [DiktatRuleSet] must
     * be created).
     *
     * For each invocation of [com.pinterest.ktlint.core.KtLintRuleEngine.lint] and [com.pinterest.ktlint.core.KtLintRuleEngine.format] the [DiktatRuleSet]
     * is retrieved.
     * This results in new instances of each [com.pinterest.ktlint.core.Rule] for each file being
     * processed.
     * As of that a [com.pinterest.ktlint.core.Rule] does not need to be thread-safe.
     *
     * However, [com.pinterest.ktlint.core.KtLintRuleEngine.format] requires the [com.pinterest.ktlint.core.Rule] to be executed twice on a
     * file in case at least one violation has been autocorrected.
     * As the same [Rule] instance is reused for the second execution of the
     * [Rule], the state of the [Rule] is shared.
     * As of this [Rule] have to clear their internal state.
     *
     * @return a default [DiktatRuleSet]
     */
    @Suppress(
        "LongMethod",
        "TOO_LONG_FUNCTION",
    )
    override fun invoke(rulesConfig: List<DiktatRuleConfig>): DiktatRuleSet {
        // Note: the order of rules is important in autocorrect mode. For example, all rules that add new code should be invoked before rules that fix formatting.
        // We don't have a way to enforce a specific order, so we should just be careful when adding new rules to this list and, when possible,
        // cover new rules in smoke test as well. If a rule needs to be at a specific position in a list, please add comment explaining it (like for NewlinesRule).
        val rules = sequenceOf(
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
            ::EmptyBlock,
            ::AvoidEmptyPrimaryConstructor,
            ::TopLevelOrderRule,
            ::SingleLineStatementsRule,
            ::MultipleModifiersSequence,
            ::TrivialPropertyAccessors,
            ::CustomGetterSetterRule,
            ::CompactInitialization,
            // other rules
            ::UseLastIndex,
            ::InlineClassesRule,
            ::ExtensionFunctionsInFileRule,
            ::CheckInverseMethodRule,
            ::StatelessClassesRule,
            ::ImplicitBackingPropertyRule,
            ::DataClassesRule,
            ::LocalVariablesRule,
            ::SmartCastRule,
            ::AvoidUtilityClass,
            ::PropertyAccessorFields,
            ::AbstractClassesRule,
            ::TrailingCommaRule,
            ::SingleInitRule,
            ::RangeConventionalRule,
            ::DebugPrintRule,
            ::CustomLabel,
            ::VariableGenericTypeDeclarationRule,
            ::LongNumericalValuesSeparatedRule,
            ::NestedFunctionBlock,
            ::AnnotationNewLineRule,
            ::SortRule,
            ::EnumsSeparated,
            ::StringConcatenationRule,
            ::StringTemplateFormatRule,
            ::AccurateCalculationsRule,
            ::CollapseIfStatementsRule,
            ::LineLength,
            ::RunInScript,
            ::TypeAliasRule,
            ::OverloadingArgumentsFunction,
            ::FunctionLength,
            ::MagicNumberRule,
            ::LambdaParameterOrder,
            ::FunctionArgumentsSize,
            ::BlankLinesRule,
            ::FileSize,
            ::AsyncAndSyncRule,
            ::NullableTypeRule,
            ::NullChecksRule,
            ::ImmutableValNoVarRule,
            ::AvoidNestedFunctionsRule,
            ::ExtensionFunctionsSameNameRule,
            ::LambdaLengthRule,
            ::BooleanExpressionsRule,
            ::ParameterNameInOuterLambdaRule,
            // formatting: moving blocks, adding line breaks, indentations etc.
            ::BlockStructureBraces,
            ::ConsecutiveSpacesRule,
            ::HeaderCommentRule,
            ::FileStructureRule,  // this rule should be right before indentation because it should operate on already valid code
            ::NewlinesRule,  // newlines need to be inserted right before fixing indentation
            ::WhiteSpaceRule,  // this rule should be after other rules that can cause wrong spacing
            ::IndentationRule,  // indentation rule should be the last because it fixes formatting after all the changes done by previous rules

        )
            .map {
                it(rulesConfig)
            }
            .toList()
        return DiktatRuleSet(rules)
    }

    override fun create(configFile: String): DiktatRuleSet = DiktatRuleSetProvider(configFile).invoke()
}
