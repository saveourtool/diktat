package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.DIKTAT_ANALYSIS_CONF
import com.pinterest.ktlint.core.Rule
import kotlin.Function0

/**
 * _KtLint_-agnostic factory which creates a [DiktatRuleSet].
 */
@Suppress("ForbiddenComment")
fun interface DiktatRuleSetFactory : Function0<DiktatRuleSet> {
    /**
     * This method is going to be called once for each file (which means if any
     * of the rules have state or are not thread-safe - a new [DiktatRuleSet] must
     * be created).
     *
     * TODO: comments for 0.47.x
     * For each invocation of [com.pinterest.ktlint.core.KtLintRuleEngine.lint] and [com.pinterest.ktlint.core.KtLintRuleEngine.format] the [DiktatRuleSet]
     * is retrieved.
     * This results in new instances of each [Rule] for each file being
     * processed.
     * As of that a [Rule] does not need to be thread-safe.
     *
     * However, [com.pinterest.ktlint.core.KtLintRuleEngine.format] requires the [Rule] to be executed twice on a
     * file in case at least one violation has been autocorrected.
     * As the same [Rule] instance is reused for the second execution of the
     * [Rule], the state of the [Rule] is shared.
     * As of this [Rule] have to clear their internal state.
     */
    override fun invoke(): DiktatRuleSet

    companion object {
        /**
         * @param diktatConfigFile the configuration file where all configurations for
         *   inspections and rules are stored.
         * @return a new instance of [DiktatRuleSetFactory].
         */
        operator fun invoke(diktatConfigFile: String = DIKTAT_ANALYSIS_CONF): DiktatRuleSetFactory =
                DefaultDiktatRuleSetFactory(diktatConfigFile)
    }
}
