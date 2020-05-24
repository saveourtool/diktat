package rri.fixbot.ruleset.huawei.rules

import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider

class HuaweiRuleSetProvider : RuleSetProvider {

    override fun get(): RuleSet = RuleSet(
        "huawei-codestyle",
        FileNaming(),
        PackageNaming(),
        IdentifierNaming()
    )
}
