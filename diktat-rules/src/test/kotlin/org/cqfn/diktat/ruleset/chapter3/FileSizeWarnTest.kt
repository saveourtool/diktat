package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.FileSize
import org.cqfn.diktat.ruleset.rules.KdocFormatting
import org.junit.Test
import java.io.File

class FileSizeWarnTest{

    private val rulesConfigListLarge: List<RulesConfig> = listOf(
        RulesConfig(Warnings.FILE_IS_TOO_LONG.name, true,
            mapOf("maxSize" to "5"))
    )

    private val rulesConfigListSmall: List<RulesConfig> = listOf(
        RulesConfig(Warnings.FILE_IS_TOO_LONG.name, true,
            mapOf("maxSize" to "10"))
    )

    private val rulesConfigListIgnore: List<RulesConfig> = listOf(
        RulesConfig(Warnings.FILE_IS_TOO_LONG.name, true,
            mapOf("maxSize" to "5", "ignoreFolders" to "main"))
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
    fun `file larger then max with ignore`() {
        val path = javaClass.classLoader.getResource("test/paragrah3/src/main/FileSizeLarger.kt")!!.path
        lintMethod(FileSize(), path, rulesConfigList = rulesConfigListIgnore)
    }

    @Test
    fun `file smaller then max`() {
        val path = javaClass.classLoader.getResource("test/paragrah3/src/main/FileSizeLarger.kt")!!.path
        lintMethod(FileSize(), path, rulesConfigList = rulesConfigListSmall)
    }

    @Test
    fun `file larger then max`() {
        val path = javaClass.classLoader.getResource("test/paragrah3/src/main/FileSizeLarger.kt")!!.path
        val file = File(path)
        lintMethod(FileSize(), path, rulesConfigList = rulesConfigListLarge)
        lintMethod(FileSize(), path,
            LintError(2, 16, "file-size",
                Warnings.FILE_IS_TOO_LONG.warnText(), false),
            rulesConfigList = rulesConfigListLarge)
    }
}
