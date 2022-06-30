package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.isRuleEnabled
import org.cqfn.diktat.common.utils.loggerWithKtlintConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.utils.getFilePath

import com.pinterest.ktlint.core.Rule
import mu.KotlinLogging
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

private typealias DiktatConfigRule = org.cqfn.diktat.common.config.rules.Rule

/**
 * This is a wrapper around Ktlint Rule
 *
 * @param id id of the rule
 * @property configRules all rules from configuration
 * @property inspections warnings that are used in the rule's code
 */
@Suppress("TooGenericExceptionCaught")
abstract class DiktatRule(
    id: String,
    val configRules: List<RulesConfig>,
    private val inspections: List<DiktatConfigRule>,
    visitorModifiers: Set<VisitorModifier> = emptySet(),
) : Rule(id, visitorModifiers) {
    /**
     * Default value is false
     */
    var isFixMode: Boolean = false

    /**
     * Will be initialized in visit
     */
    lateinit var emitWarn: EmitType

    @Suppress("TooGenericExceptionThrown")
    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitType
    ) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (areInspectionsDisabled()) {
            return
        } else {
            try {
                logic(node)
            } catch (internalError: Throwable) {
                log.error(
                    """Internal error has occurred in rule [$id]. Please make an issue on this bug at https://github.com/saveourtool/diKTat/.
                       As a workaround you can disable these inspections in yml config: <$inspections>.
                       Root cause of the problem is in [${node.getFilePath()}] file.
                    """.trimIndent(), internalError
                )
                // we are very sorry for throwing common Error here, but unfortunately we are not able to throw
                // any existing Exception, as they will be caught in ktlint framework and the logging will be confusing:
                // in this case it will incorrectly ask you to report issues in diktat to ktlint repository
                throw Error("Internal error in diktat application")
            }
        }
    }

    private fun areInspectionsDisabled(): Boolean =
        inspections.none { configRules.isRuleEnabled(it) }

    /**
     * Logic of the rule
     *
     * @param node node that are coming from visit
     */
    abstract fun logic(node: ASTNode)

    companion object {
        private val log = KotlinLogging.loggerWithKtlintConfig(DiktatRule::class)
    }
}
