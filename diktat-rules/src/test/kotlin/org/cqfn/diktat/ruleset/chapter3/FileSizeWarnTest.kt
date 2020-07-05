package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import org.assertj.core.api.Assertions
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.FileSize
import org.cqfn.diktat.ruleset.utils.DiktatRuleSetProviderTest
import org.junit.Test
import java.io.File

class FileSizeWarnTest {

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
                   code: String,
                   vararg lintErrors: LintError,
                   rulesConfigList: List<RulesConfig>? = emptyList()) {
        val res = mutableListOf<LintError>()
        KtLint.lint(
                KtLint.Params(
                        fileName = fileName,
                        text = code,
                        ruleSets = listOf(DiktatRuleSetProviderTest(rule, rulesConfigList).get()),
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
        val path = javaClass.classLoader.getResource("test/paragraph3/src/main/FileSizeLarger.kt")
        val file = File(path!!.file)
        lintMethod(FileSize(), file.absolutePath, file.readText(), rulesConfigList = rulesConfigListIgnore)
    }

    @Test
    fun `file smaller then max`() {
        val path = javaClass.classLoader.getResource("test/paragraph3/src/main/FileSizeLarger.kt")
        val file = File(path!!.file)
        lintMethod(FileSize(), file.absolutePath, file.readText(), rulesConfigList = rulesConfigListSmall)
    }

    @Test
    fun `file larger then max`() {
        val path = javaClass.classLoader.getResource("test/paragraph3/src/main/FileSizeLarger.kt")
        val file = File(path!!.file)
        val size = file.readText().split("\n").size
        lintMethod(FileSize(), file.absolutePath, file.readText(),
                LintError(1, 1, "$DIKTAT_RULE_SET_ID:file-size",
                        "${Warnings.FILE_IS_TOO_LONG.warnText()} $size", false),
                rulesConfigList = rulesConfigListLarge)
    }
}
