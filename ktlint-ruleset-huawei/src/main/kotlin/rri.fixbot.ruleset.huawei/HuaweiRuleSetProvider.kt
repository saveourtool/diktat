package rri.fixbot.ruleset.huawei

import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider

class HuaweiRuleSetProvider : RuleSetProvider {

    override fun get(): RuleSet = RuleSet(
        "huawei-codestyle",
        PackageNaming1_3()
    )
}
