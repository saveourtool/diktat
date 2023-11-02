package com.saveourtool.diktat.ktlint

import com.saveourtool.diktat.api.DiktatRule
import com.saveourtool.diktat.api.DiktatRuleSet
import com.saveourtool.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import com.pinterest.ktlint.rule.engine.core.api.Rule
import com.pinterest.ktlint.rule.engine.core.api.Rule.VisitorModifier.RunAfterRule.Mode
import com.pinterest.ktlint.rule.engine.core.api.RuleId
import com.pinterest.ktlint.rule.engine.core.api.RuleProvider
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType
import java.nio.file.Paths
import java.util.concurrent.atomic.AtomicInteger
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.readText
import kotlin.io.path.writeText

private typealias EmitType = (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit

/**
 * This is a wrapper around __KtLint__'s [Rule] which adjusts visitorModifiers to keep order with prevRule.
 * @property rule
 */
class KtLintRuleWrapper(
    val rule: DiktatRule,
    prevRuleId: RuleId? = null,
) : Rule(
    ruleId = rule.id.toRuleId(DIKTAT_RULE_SET_ID),
    about = about,
    visitorModifiers = createVisitorModifiers(rule, prevRuleId),
) {
    override fun beforeVisitChildNodes(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitType,
    ) {
        node.dumpNodeIfRequired(rule, true)
        rule.invoke(node, autoCorrect) { offset, errorMessage, canBeAutoCorrected ->
            emit.invoke(offset, errorMessage.correctErrorDetail(canBeAutoCorrected), canBeAutoCorrected)
        }

    }

    override fun afterVisitChildNodes(node: ASTNode, autoCorrect: Boolean, emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        super.afterVisitChildNodes(node, autoCorrect, emit)
        node.dumpNodeIfRequired(rule, false)
    }

    companion object {
        private val counter = AtomicInteger(1000)

        private val folder = Paths.get("d:\\projects\\diktat\\test")

        private fun ASTNode.dumpNodeIfRequired(rule: DiktatRule, isBefore: Boolean) {
            if (elementType == KtFileElementType.INSTANCE) {
                val newContent = prettyPrint()
                val latestContent = folder.listDirectoryEntries()
                    .find { it.name.startsWith("${counter.get()}") }
                    ?.readText()
                if (latestContent != newContent) {
                    folder.resolve("${counter.incrementAndGet()}_${if (isBefore) "before" else "after"}_${rule.javaClass.simpleName}.txt")
                        .writeText(newContent)
                }
            }
        }

        private val about: About = About(
            maintainer = "Diktat",
            repositoryUrl = "https://github.com/saveourtool/diktat",
            issueTrackerUrl = "https://github.com/saveourtool/diktat/issues",
        )

        private fun Sequence<DiktatRule>.wrapRulesToProviders(): Sequence<RuleProvider> = runningFold(null as RuleProvider?) { prevRuleProvider, diktatRule ->
            val prevRuleId = prevRuleProvider?.ruleId?.value?.toRuleId(DIKTAT_RULE_SET_ID)
            RuleProvider(
                provider = { KtLintRuleWrapper(diktatRule, prevRuleId) },
            )
        }.filterNotNull()

        /**
         * @return [Set] of __KtLint__'s [RuleProvider]s created from [DiktatRuleSet]
         */
        fun DiktatRuleSet.toKtLint(): Set<RuleProvider> = rules
            .asSequence()
            .wrapRulesToProviders()
            .toSet()

        private fun createVisitorModifiers(
            rule: DiktatRule,
            prevRuleId: RuleId?,
        ): Set<VisitorModifier> = prevRuleId?.run {
            val ruleId = rule.id.toRuleId(DIKTAT_RULE_SET_ID)
            require(ruleId != prevRuleId) {
                "PrevRule has same ID as rule: $ruleId"
            }
            setOf(
                VisitorModifier.RunAfterRule(
                    ruleId = prevRuleId,
                    mode = Mode.REGARDLESS_WHETHER_RUN_AFTER_RULE_IS_LOADED_OR_DISABLED
                )
            )
        } ?: emptySet()

        /**
         * @return a rule to which a logic is delegated
         */
        internal fun Rule.unwrap(): DiktatRule = (this as? KtLintRuleWrapper)?.rule ?: error("Provided rule ${javaClass.simpleName} is not wrapped by diktat")


        /**
         * Converts this AST node and all its children to pretty string representation
         */
        @Suppress("AVOID_NESTED_FUNCTIONS")
        fun ASTNode.prettyPrint(level: Int = 0, maxLevel: Int = -1): String {
            /**
             * AST operates with \n only, so we need to build the whole string representation and then change line separator
             */
            fun ASTNode.doPrettyPrint(level: Int, maxLevel: Int): String {
                val result = StringBuilder("${this.elementType}: \"${this.text}\"").append('\n')
                if (maxLevel != 0) {
                    this.getChildren(null).forEach { child ->
                        result.append(
                            "${"-".repeat(level + 1)} " +
                                    child.doPrettyPrint(level + 1, maxLevel - 1)
                        )
                    }
                }
                return result.toString()
            }
            return doPrettyPrint(level, maxLevel).replace("\n", System.lineSeparator())
        }
    }
}
