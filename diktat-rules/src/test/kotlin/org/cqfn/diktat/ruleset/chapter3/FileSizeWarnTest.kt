package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.FileSize
import org.junit.Test

class FileSizeWarnTest{

    private val rulesConfigList: List<RulesConfig> = listOf(
        RulesConfig(Warnings.FILE_SIZE_LARGER.name, true,
            mapOf("maxSize" to "10", "ignorFolders" to "packagee"))
    )
    fun lintMethod(rule: Rule,
                   fileName: String,
                   vararg lintErrors: LintError,
                   rulesConfigList: List<RulesConfig>? = emptyList()) {
        val res = mutableListOf<LintError>()
        KtLint.lint(
            KtLint.Params(
                fileName = fileName,
                text = "",
                ruleSets = listOf(RuleSet("standard", rule)),
                rulesConfigList = rulesConfigList,
                cb = { e, _ -> res.add(e) }
            )
        )
        Assertions.assertThat(
            res
        ).containsExactly(
            *lintErrors
        )
    }

    @Test
    fun `file larger then expected`() {
        val path = javaClass.classLoader.getResource("test/paragrah3/FileSizeLarger.kt")!!.path
        lintMethod(FileSize(), path, rulesConfigList = rulesConfigList)
    }
}
