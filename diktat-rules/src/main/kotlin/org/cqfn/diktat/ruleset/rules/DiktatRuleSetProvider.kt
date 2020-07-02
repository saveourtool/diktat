package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider

class DiktatRuleSetProvider : RuleSetProvider {
    override fun get(): RuleSet = RuleSet(
        "diktat-ruleset",
        KdocComments(),
        KdocMethods(),
        KdocFormatting(),
        FileNaming(),
        FileSize(),
        PackageNaming(),
        IdentifierNaming()
    )
}
