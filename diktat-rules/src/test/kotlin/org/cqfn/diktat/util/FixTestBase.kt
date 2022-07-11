package org.cqfn.diktat.util

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.test.framework.processing.FileComparisonResult
import org.cqfn.diktat.test.framework.processing.TestComparatorUnit
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSetProvider
import org.apache.commons.io.FileUtils.copyFile
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Assertions
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.bufferedWriter
import kotlin.io.path.div
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.pathString

/**
 * @property resourceFilePath path to files which will be compared in tests
 */
open class FixTestBase(
    protected val resourceFilePath: String,
    private val ruleSetProviderRef: (rulesConfigList: List<RulesConfig>?) -> RuleSetProvider,
    private val cb: LintErrorCallback = defaultCallback,
    private val rulesConfigList: List<RulesConfig>? = null,
) {
    private val testComparatorUnit = TestComparatorUnit(resourceFilePath) { text, fileName ->
        format(ruleSetProviderRef, text, fileName, rulesConfigList, cb = cb)
    }

    constructor(resourceFilePath: String,
                ruleSupplier: (rulesConfigList: List<RulesConfig>) -> Rule,
                rulesConfigList: List<RulesConfig>? = null,
                cb: LintErrorCallback = defaultCallback
    ) : this(
        resourceFilePath,
        { overrideRulesConfigList -> DiktatRuleSetProvider4Test(ruleSupplier, overrideRulesConfigList) },
        cb,
        rulesConfigList
    )

    /**
     * @param expectedPath path to file with expected result, relative to [resourceFilePath]
     * @param testPath path to file with code that will be transformed by formatter, relative to [resourceFilePath]
     */
    protected fun fixAndCompare(expectedPath: String, testPath: String) {
        Assertions.assertTrue(
            testComparatorUnit
                .compareFilesFromResources(expectedPath, testPath)
        )
    }

    /**
     * @param expectedPath path to file with expected result, relative to [resourceFilePath]
     * @param testPath path to file with code that will be transformed by formatter, relative to [resourceFilePath]
     */
    protected fun fixAndCompareSmokeTest(expectedPath: String, testPath: String) {
        Assertions.assertTrue(
            testComparatorUnit
                .compareFilesFromResources(expectedPath, testPath, true)
        )
    }

    private fun getSaveForCurrentOs() = when {
        System.getProperty("os.name").startsWith("Linux", ignoreCase = true) -> "save-$SAVE_VERSION-linuxX64.kexe"
        System.getProperty("os.name").startsWith("Mac", ignoreCase = true) -> "save-$SAVE_VERSION-macosX64.kexe"
        System.getProperty("os.name").startsWith("Windows", ignoreCase = true) -> "save-$SAVE_VERSION-mingwX64.exe"
        else -> ""
    }

    private fun getProcessBuilder(expectedPath: String, testPath: String) =  when {
        System.getProperty("os.name").startsWith("Linux", ignoreCase = true) -> ProcessBuilder("chmod", "777", "src/test/resources/test/smoke/${getSaveForCurrentOs()}", "&", "src/test/resources/test/smoke/${getSaveForCurrentOs()}", "src/test/resources/test/smoke/src/main/kotlin", expectedPath, testPath)
        System.getProperty("os.name").startsWith("Mac", ignoreCase = true) -> ProcessBuilder("chmod", "777", "src/test/resources/test/smoke/${getSaveForCurrentOs()}", "&", "src/test/resources/test/smoke/${getSaveForCurrentOs()}", "src/test/resources/test/smoke/src/main/kotlin", expectedPath, testPath)
        else -> ProcessBuilder("src/test/resources/test/smoke/${getSaveForCurrentOs()}", "src/test/resources/test/smoke/src/main/kotlin", expectedPath, testPath)
    }

    private fun downloadFile(url: String, file: File) {
        val httpClient = HttpClients.createDefault()
        val request = HttpGet(url)
        httpClient.use {
            val response: CloseableHttpResponse = httpClient.execute(request)
            response.use {
                val fileSave = response.entity
                fileSave?.let {
                    FileOutputStream(file).use { outstream -> fileSave.writeTo(outstream) }
                }
            }
        }
    }

    /**
     * @param expectedPath path to file with expected result, relative to [resourceFilePath]
     * @param testPath path to file with code that will be transformed by formatter, relative to [resourceFilePath]
     * @param configFilePath path of diktat-analysis file
     */
    @Suppress("TOO_LONG_FUNCTION")
    protected fun saveSmokeTest(
        configFilePath: String,
        expectedPath: String,
        testPath: String
    ) {
        val processBuilder = getProcessBuilder(expectedPath, testPath)

        val diktatDir: String =
            Paths.get("../diktat-ruleset/target")
                .takeIf { it.exists() }
                ?.listDirectoryEntries()
                ?.single { it.name.contains("diktat") }
                ?.pathString ?: ""

        val file = File("tmpSave.txt")
        val diktat = File("src/test/resources/test/smoke/diktat.jar")
        val configFile = File("src/test/resources/test/smoke/diktat-analysis.yml")
        val diktatFrom = File(diktatDir)
        val save = File("src/test/resources/test/smoke/${getSaveForCurrentOs()}")
        val ktlint = File("src/test/resources/test/smoke/ktlint")

        val configFileFrom = File(configFilePath)

        ktlint.createNewFile()
        save.createNewFile()
        file.createNewFile()
        diktat.createNewFile()
        configFile.createNewFile()

        copyFile(diktatFrom, diktat)
        copyFile(configFileFrom, configFile)

        processBuilder.redirectOutput(file)

        downloadFile("https://github.com/saveourtool/save-cli/releases/download/v$SAVE_VERSION/${getSaveForCurrentOs()}", save)
        downloadFile("https://github.com/pinterest/ktlint/releases/download/$KTLINT_VERSION/ktlint", ktlint)

        val process = processBuilder.start()
        process.waitFor()

        val output = file.readLines()
        val saveOutput = output.joinToString("\n")

        file.delete()
        diktat.delete()
        configFile.delete()
        ktlint.delete()
        save.delete()

        Assertions.assertTrue(
            saveOutput.contains("SUCCESS")
        )
    }

    /**
     * @param expectedPath path to file with expected result, relative to [resourceFilePath]
     * @param testPath path to file with code that will be transformed by formatter, relative to [resourceFilePath]
     * @param overrideRulesConfigList optional override to [rulesConfigList]
     * @see fixAndCompareContent
     */
    protected fun fixAndCompare(expectedPath: String,
                                testPath: String,
                                overrideRulesConfigList: List<RulesConfig>
    ) {
        val testComparatorUnit = TestComparatorUnit(resourceFilePath) { text, fileName ->
            format(ruleSetProviderRef, text, fileName, overrideRulesConfigList)
        }
        Assertions.assertTrue(
            testComparatorUnit
                .compareFilesFromResources(expectedPath, testPath)
        )
    }

    /**
     * Unlike [fixAndCompare], this method doesn't perform any assertions.
     *
     * @param actualContent the original file content (may well be modified as
     *   fixes are applied).
     * @param expectedContent the content the file is expected to have after the
     *   fixes are applied.
     * @param tempDir the temporary directory (usually injected by _JUnit_).
     * @param overrideRulesConfigList an optional override for [rulesConfigList]
     *   (the class-wide configuration).
     * @return the result of file content comparison.
     * @see fixAndCompare
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    protected fun fixAndCompareContent(
        @Language("kotlin") actualContent: String,
        @Language("kotlin") expectedContent: String = actualContent,
        tempDir: Path,
        overrideRulesConfigList: List<RulesConfig>? = null
    ): FileComparisonResult {
        val actual = tempDir / "actual.kt"
        actual.bufferedWriter().use { out ->
            out.write(actualContent)
        }

        val expected = tempDir / "expected.kt"
        expected.bufferedWriter().use { out ->
            out.write(expectedContent)
        }

        val testComparatorUnit = TestComparatorUnit(tempDir.toString()) { text, fileName ->
            format(ruleSetProviderRef, text, fileName, overrideRulesConfigList ?: rulesConfigList, cb)
        }

        return testComparatorUnit.compareFilesFromFileSystem(expected, actual)
    }

    companion object {
        private const val KTLINT_VERSION = "0.46.1"
        private const val SAVE_VERSION = "0.3.1"
    }
}
