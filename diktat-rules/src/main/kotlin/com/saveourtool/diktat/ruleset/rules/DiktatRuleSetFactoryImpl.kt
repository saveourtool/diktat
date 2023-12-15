package com.saveourtool.diktat.ruleset.rules

import com.saveourtool.diktat.api.DiktatRuleConfig
import com.saveourtool.diktat.api.DiktatRuleSet
import com.saveourtool.diktat.api.DiktatRuleSetFactory
import com.saveourtool.diktat.ruleset.rules.chapter1.FileNaming
import com.saveourtool.diktat.ruleset.rules.chapter1.IdentifierNaming
import com.saveourtool.diktat.ruleset.rules.chapter1.PackageNaming
import com.saveourtool.diktat.ruleset.rules.chapter2.comments.CommentsRule
import com.saveourtool.diktat.ruleset.rules.chapter2.comments.HeaderCommentRule
import com.saveourtool.diktat.ruleset.rules.chapter2.kdoc.CommentsFormatting
import com.saveourtool.diktat.ruleset.rules.chapter2.kdoc.KdocComments
import com.saveourtool.diktat.ruleset.rules.chapter2.kdoc.KdocFormatting
import com.saveourtool.diktat.ruleset.rules.chapter2.kdoc.KdocMethods
import com.saveourtool.diktat.ruleset.rules.chapter3.AnnotationNewLineRule
import com.saveourtool.diktat.ruleset.rules.chapter3.BlockStructureBraces
import com.saveourtool.diktat.ruleset.rules.chapter3.BooleanExpressionsRule
import com.saveourtool.diktat.ruleset.rules.chapter3.BracesInConditionalsAndLoopsRule
import com.saveourtool.diktat.ruleset.rules.chapter3.ClassLikeStructuresOrderRule
import com.saveourtool.diktat.ruleset.rules.chapter3.CollapseIfStatementsRule
import com.saveourtool.diktat.ruleset.rules.chapter3.ConsecutiveSpacesRule
import com.saveourtool.diktat.ruleset.rules.chapter3.DebugPrintRule
import com.saveourtool.diktat.ruleset.rules.chapter3.EmptyBlock
import com.saveourtool.diktat.ruleset.rules.chapter3.EnumsSeparated
import com.saveourtool.diktat.ruleset.rules.chapter3.LineLength
import com.saveourtool.diktat.ruleset.rules.chapter3.LongNumericalValuesSeparatedRule
import com.saveourtool.diktat.ruleset.rules.chapter3.MagicNumberRule
import com.saveourtool.diktat.ruleset.rules.chapter3.MultipleModifiersSequence
import com.saveourtool.diktat.ruleset.rules.chapter3.NullableTypeRule
import com.saveourtool.diktat.ruleset.rules.chapter3.PreviewAnnotationRule
import com.saveourtool.diktat.ruleset.rules.chapter3.RangeConventionalRule
import com.saveourtool.diktat.ruleset.rules.chapter3.SingleLineStatementsRule
import com.saveourtool.diktat.ruleset.rules.chapter3.SortRule
import com.saveourtool.diktat.ruleset.rules.chapter3.StringConcatenationRule
import com.saveourtool.diktat.ruleset.rules.chapter3.StringTemplateFormatRule
import com.saveourtool.diktat.ruleset.rules.chapter3.TrailingCommaRule
import com.saveourtool.diktat.ruleset.rules.chapter3.WhenMustHaveElseRule
import com.saveourtool.diktat.ruleset.rules.chapter3.files.BlankLinesRule
import com.saveourtool.diktat.ruleset.rules.chapter3.files.FileSize
import com.saveourtool.diktat.ruleset.rules.chapter3.files.FileStructureRule
import com.saveourtool.diktat.ruleset.rules.chapter3.files.IndentationRule
import com.saveourtool.diktat.ruleset.rules.chapter3.files.NewlinesRule
import com.saveourtool.diktat.ruleset.rules.chapter3.files.SemicolonsRule
import com.saveourtool.diktat.ruleset.rules.chapter3.files.TopLevelOrderRule
import com.saveourtool.diktat.ruleset.rules.chapter3.files.WhiteSpaceRule
import com.saveourtool.diktat.ruleset.rules.chapter3.identifiers.LocalVariablesRule
import com.saveourtool.diktat.ruleset.rules.chapter4.ImmutableValNoVarRule
import com.saveourtool.diktat.ruleset.rules.chapter4.NullChecksRule
import com.saveourtool.diktat.ruleset.rules.chapter4.SmartCastRule
import com.saveourtool.diktat.ruleset.rules.chapter4.TypeAliasRule
import com.saveourtool.diktat.ruleset.rules.chapter4.VariableGenericTypeDeclarationRule
import com.saveourtool.diktat.ruleset.rules.chapter4.calculations.AccurateCalculationsRule
import com.saveourtool.diktat.ruleset.rules.chapter5.AsyncAndSyncRule
import com.saveourtool.diktat.ruleset.rules.chapter5.AvoidNestedFunctionsRule
import com.saveourtool.diktat.ruleset.rules.chapter5.CheckInverseMethodRule
import com.saveourtool.diktat.ruleset.rules.chapter5.CustomLabel
import com.saveourtool.diktat.ruleset.rules.chapter5.FunctionArgumentsSize
import com.saveourtool.diktat.ruleset.rules.chapter5.FunctionLength
import com.saveourtool.diktat.ruleset.rules.chapter5.LambdaLengthRule
import com.saveourtool.diktat.ruleset.rules.chapter5.LambdaParameterOrder
import com.saveourtool.diktat.ruleset.rules.chapter5.NestedFunctionBlock
import com.saveourtool.diktat.ruleset.rules.chapter5.OverloadingArgumentsFunction
import com.saveourtool.diktat.ruleset.rules.chapter5.ParameterNameInOuterLambdaRule
import com.saveourtool.diktat.ruleset.rules.chapter6.AvoidEmptyPrimaryConstructor
import com.saveourtool.diktat.ruleset.rules.chapter6.AvoidUtilityClass
import com.saveourtool.diktat.ruleset.rules.chapter6.CustomGetterSetterRule
import com.saveourtool.diktat.ruleset.rules.chapter6.ExtensionFunctionsInFileRule
import com.saveourtool.diktat.ruleset.rules.chapter6.ExtensionFunctionsSameNameRule
import com.saveourtool.diktat.ruleset.rules.chapter6.ImplicitBackingPropertyRule
import com.saveourtool.diktat.ruleset.rules.chapter6.PropertyAccessorFields
import com.saveourtool.diktat.ruleset.rules.chapter6.RunInScript
import com.saveourtool.diktat.ruleset.rules.chapter6.TrivialPropertyAccessors
import com.saveourtool.diktat.ruleset.rules.chapter6.UseLastIndex
import com.saveourtool.diktat.ruleset.rules.chapter6.UselessSupertype
import com.saveourtool.diktat.ruleset.rules.chapter6.classes.AbstractClassesRule
import com.saveourtool.diktat.ruleset.rules.chapter6.classes.CompactInitialization
import com.saveourtool.diktat.ruleset.rules.chapter6.classes.DataClassesRule
import com.saveourtool.diktat.ruleset.rules.chapter6.classes.InlineClassesRule
import com.saveourtool.diktat.ruleset.rules.chapter6.classes.SingleConstructorRule
import com.saveourtool.diktat.ruleset.rules.chapter6.classes.SingleInitRule
import com.saveourtool.diktat.ruleset.rules.chapter6.classes.StatelessClassesRule

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
            // semicolons, comments, documentation
            ::SemicolonsRule,
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
            ::PreviewAnnotationRule,
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
}
