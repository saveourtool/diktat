package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.cqfn.diktat.ruleset.rules.files.FileStructureRule

class DiktatRuleSetProvider : RuleSetProvider {
    override fun get(): RuleSet = RuleSet(
        "diktat-ruleset",
        KdocComments(),
        KdocMethods(),
        KdocFormatting(),
        FileNaming(),
        PackageNaming(),
        IdentifierNaming(),
        FileStructureRule()  // this rule should be the last because it should operate on already valid code
    )
}
