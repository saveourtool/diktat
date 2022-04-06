package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.chapter3.files.FileSize
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

import java.io.File

class FileSizeWarnTest : LintTestBase(::FileSize) {
    val rulied = "$DIKTAT_RULE_SET_ID:${FileSize.nameId}"

    private val rulesConfigListLarge: List<RulesConfig> = listOf(
        RulesConfig(Warnings.FILE_IS_TOO_LONG.name, true,
            mapOf("maxSize" to "5"))
    )
    private val rulesConfigListSmall: List<RulesConfig> = listOf(
        RulesConfig(Warnings.FILE_IS_TOO_LONG.name, true,
            mapOf("maxSize" to "10"))
    )
    private val rulesConfigListEmpty: List<RulesConfig> = listOf(
        RulesConfig(Warnings.FILE_IS_TOO_LONG.name, true,
            emptyMap())
    )

    @Test
    @Tag(WarningNames.FILE_IS_TOO_LONG)
    fun `file smaller then max`() {
        val path = javaClass.classLoader.getResource("test/paragraph3/src/main/FileSizeLarger.kt")
        val file = File(path!!.file)
        lintMethod(file.readText(), fileName = file.absolutePath, rulesConfigList = rulesConfigListSmall)
    }

    @Test
    @Tag(WarningNames.FILE_IS_TOO_LONG)
    fun `file larger then max`() {
        val path = javaClass.classLoader.getResource("test/paragraph3/src/main/FileSizeLarger.kt")
        val file = File(path!!.file)
        val size = file
            .readText()
            .split("\n")
            .size
        lintMethod(file.readText(),
            LintError(1, 1, rulied,
                "${Warnings.FILE_IS_TOO_LONG.warnText()} $size", false),
            fileName = file.absolutePath,
            rulesConfigList = rulesConfigListLarge)
    }

    @Test
    @Tag(WarningNames.FILE_IS_TOO_LONG)
    fun `use default values`() {
        val path = javaClass.classLoader.getResource("test/paragraph3/src/main/FileSizeLarger.kt")
        val file = File(path!!.file)
        lintMethod(file.readText(), fileName = file.absolutePath, rulesConfigList = rulesConfigListEmpty)
    }

    @Test
    @Tag(WarningNames.FILE_IS_TOO_LONG)
    fun `file has more than 2000 lines`() {
        val path = javaClass.classLoader.getResource("test/paragraph3/src/main/A/FileSize2000.kt")
        val file = File(path!!.file)
        val size = generate2000lines() + 1
        lintMethod(file.readText(),
            LintError(1, 1, rulied,
                "${Warnings.FILE_IS_TOO_LONG.warnText()} $size", false),
            fileName = file.absolutePath, rulesConfigList = rulesConfigListLarge)
    }

    private fun generate2000lines(): Int {
        val path = javaClass.classLoader.getResource("test/paragraph3/src/main/A/FileSize2000.kt")
        val file = File(path!!.file)
        file.writeText("//hello \n".repeat(2000))
        return 2000
    }
}
