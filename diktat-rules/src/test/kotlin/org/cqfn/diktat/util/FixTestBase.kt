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
import kotlin.io.path.bufferedWriter
import kotlin.io.path.div

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
    private val diktatVersion = "1.2.1-SNAPSHOT"

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
        System.getProperty("os.name").startsWith("Linux", ignoreCase = true) -> "save-0.3.1-linuxX64.kexe"
        System.getProperty("os.name").startsWith("Mac", ignoreCase = true) -> "save-0.3.1-macosX64.kexe"
        System.getProperty("os.name").startsWith("Windows", ignoreCase = true) -> "save-0.3.1-mingwX64.exe"
        else -> ""
    }

    private fun downloadFile(url: String, file: File) {
        val httpClient = HttpClients.createDefault()
        val request = HttpGet(url)
        val response: CloseableHttpResponse = httpClient.execute(request)
        val fileSave = response.entity
        fileSave?.let {
            FileOutputStream(file).use { outstream -> fileSave.writeTo(outstream) }
        }
    }

    /**
     * @param expectedPath path to file with expected result, relative to [resourceFilePath]
     * @param testPath path to file with code that will be transformed by formatter, relative to [resourceFilePath]
     * @param configFilePath path of diktat-analysis file
     */
    protected fun saveSmokeTest(
        configFilePath: String,
        expectedPath: String,
        testPath: String
    ) {
        val processBuilder = ProcessBuilder("src/test/resources/test/smoke/${getSaveForCurrentOs()}", "src/test/resources/test/smoke/src/main/kotlin", expectedPath, testPath)

        val file = File("tmpSave.txt")
        val diktat = File("src/test/resources/test/smoke/diktat.jar")
        val configFile = File("src/test/resources/test/smoke/diktat-analysis.yml")
        val diktatFrom = File("../diktat-ruleset/target/diktat-$diktatVersion.jar")
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

        downloadFile("https://github.com/saveourtool/save-cli/releases/download/v0.3.1/${getSaveForCurrentOs()}", save)
        downloadFile("https://github.com/pinterest/ktlint/releases/download/0.46.1/ktlint", ktlint)

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
}
