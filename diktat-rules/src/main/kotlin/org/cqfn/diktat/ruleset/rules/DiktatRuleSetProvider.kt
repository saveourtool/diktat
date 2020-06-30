package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.cqfn.diktat.ruleset.rules.comments.CommentsRule

class DiktatRuleSetProvider : RuleSetProvider {
    override fun get(): RuleSet = RuleSet(
        "diktat-ruleset",
        CommentsRule(),
        KdocComments(),
        KdocMethods(),
        KdocFormatting(),
        FileNaming(),
        PackageNaming(),
        IdentifierNaming()
    )
}
