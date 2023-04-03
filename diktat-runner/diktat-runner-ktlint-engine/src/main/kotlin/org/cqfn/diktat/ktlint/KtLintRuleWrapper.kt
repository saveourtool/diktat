package org.cqfn.diktat.ktlint

import org.cqfn.diktat.api.DiktatRule
import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.common.config.rules.qualifiedWithRuleSetId
import com.pinterest.ktlint.core.Rule
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

private typealias EmitType = (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit

/**
 * This is a wrapper around __KtLint__'s [Rule] which adjusts visitorModifiers to keep order with prevRule.
 * @property rule
 */
class KtLintRuleWrapper(
    val rule: DiktatRule,
    prevRule: KtLintRuleWrapper? = null,
) : Rule(
    id = rule.id.qualifiedWithRuleSetId(DIKTAT_RULE_SET_ID),
    visitorModifiers = createVisitorModifiers(rule, prevRule),
) {
    @Deprecated(
        "Marked for deletion in ktlint 0.48.0",
        replaceWith = ReplaceWith("beforeVisitChildNodes(node, autoCorrect, emit)"),
    )
    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitType,
    ) = rule.invoke(node, autoCorrect, emit)

    companion object {
        private fun createVisitorModifiers(
            rule: DiktatRule,
            prevRule: KtLintRuleWrapper?,
        ): Set<VisitorModifier> = prevRule?.id?.qualifiedWithRuleSetId(DIKTAT_RULE_SET_ID)
            ?.let { previousRuleId ->
                val ruleId = rule.id.qualifiedWithRuleSetId(DIKTAT_RULE_SET_ID)
                require(ruleId != previousRuleId) {
                    "PrevRule has same ID as rule: $ruleId"
                }
                setOf(
                    VisitorModifier.RunAfterRule(
                        ruleId = previousRuleId,
                        loadOnlyWhenOtherRuleIsLoaded = false,
                        runOnlyWhenOtherRuleIsEnabled = false
                    )
                )
            } ?: emptySet()

        /**
         * @return a rule to which a logic is delegated
         */
        internal fun Rule.unwrap(): DiktatRule = (this as? KtLintRuleWrapper)?.rule ?: error("Provided rule ${javaClass.simpleName} is not wrapped by diktat")
    }
}
