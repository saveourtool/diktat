package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import org.assertj.core.api.Assertions
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.files.FileSize
import org.cqfn.diktat.util.DiktatRuleSetProvider4Test
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

    private val rulesConfigListEmpty: List<RulesConfig> = listOf(
            RulesConfig(Warnings.FILE_IS_TOO_LONG.name, true,
            mapOf())
    )

    private val rulesConfigListOnlyIgnore: List<RulesConfig> = listOf(
            RulesConfig(Warnings.FILE_IS_TOO_LONG.name, true,
                    mapOf("ignoreFolders" to "A"))
    )

    private val rulesConfigListTwoIgnoreFolders: List<RulesConfig> = listOf(
        RulesConfig(Warnings.FILE_IS_TOO_LONG.name, true,
        mapOf("maxSize" to "8", "ignoreFolders" to "A, B"))
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
                        ruleSets = listOf(DiktatRuleSetProvider4Test(rule, rulesConfigList).get()),
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

    @Test
    fun `use default values`(){
        val path = javaClass.classLoader.getResource("test/paragraph3/src/main/FileSizeLarger.kt")
        val file = File(path!!.file)
        lintMethod(FileSize(), file.absolutePath, file.readText(), rulesConfigList = rulesConfigListEmpty)
    }

    @Test
    fun `file has more than 2000 lines`(){
        val path = javaClass.classLoader.getResource("test/paragraph3/src/main/A/FileSize2000.kt")
        val file = File(path!!.file)
        val size = generate2000lines() + 1
        lintMethod(FileSize(), file.absolutePath, file.readText(),
                LintError(1, 1, "$DIKTAT_RULE_SET_ID:file-size",
                        "${Warnings.FILE_IS_TOO_LONG.warnText()} $size", false),
                rulesConfigList = rulesConfigListLarge)
    }

    @Test
    fun `config has only ignoreFolders`(){
        val path = javaClass.classLoader.getResource("test/paragraph3/src/main/A/FileSize2000.kt")
        val file = File(path!!.file)
        lintMethod(FileSize(), file.absolutePath, file.readText(), rulesConfigList = rulesConfigListOnlyIgnore)
    }

    private fun generate2000lines(): Int{
        val path = javaClass.classLoader.getResource("test/paragraph3/src/main/A/FileSize2000.kt")
        val file = File(path!!.file)
        file.writeText("//hello \n".repeat(2000))
        return 2000
    }

    @Test
    fun `ignoring two out of three folders`(){
        var path = javaClass.classLoader.getResource("test/paragraph3/src/main/A/FileSizeA.kt")
        var file = File(path!!.file)
        lintMethod(FileSize(), file.absolutePath, file.readText(), rulesConfigList = rulesConfigListTwoIgnoreFolders)
        path = javaClass.classLoader.getResource("test/paragraph3/src/main/B/FileSizeB.kt")
        file = File(path!!.file)
        lintMethod(FileSize(), file.absolutePath, file.readText(), rulesConfigList = rulesConfigListTwoIgnoreFolders)
        path = javaClass.classLoader.getResource("test/paragraph3/src/main/C/FileSizeC.kt")
        file = File(path!!.file)
        val size = 10
        lintMethod(FileSize(), file.absolutePath, file.readText(),
                LintError(1, 1, "$DIKTAT_RULE_SET_ID:file-size",
                        "${Warnings.FILE_IS_TOO_LONG.warnText()} $size", false),
                rulesConfigList = rulesConfigListTwoIgnoreFolders)
    }
}
