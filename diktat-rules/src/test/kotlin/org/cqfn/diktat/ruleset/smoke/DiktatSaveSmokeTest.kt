package org.cqfn.diktat.ruleset.smoke

import org.apache.commons.io.FileUtils
import org.apache.http.client.methods.CloseableHttpResponse
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.client.HttpClients
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.pathString

class DiktatSaveSmokeTest : DiktatSmokeTestBase() {
    override fun fixAndCompareBase(
        config: String,
        test: String,
        expected: String
    ) {
        saveSmokeTest(config, test, expected)
    }

    companion object {
        private const val KTLINT_VERSION = "0.46.1"
        private const val SAVE_VERSION = "0.3.1"

        private fun getSaveForCurrentOs() = when {
            System.getProperty("os.name").startsWith("Linux", ignoreCase = true) -> "save-$SAVE_VERSION-linuxX64.kexe"
            System.getProperty("os.name").startsWith("Mac", ignoreCase = true) -> "save-$SAVE_VERSION-macosX64.kexe"
            System.getProperty("os.name").startsWith("Windows", ignoreCase = true) -> "save-$SAVE_VERSION-mingwX64.exe"
            else -> ""
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

        @BeforeAll
        @JvmStatic
        internal fun beforeAll() {
            val diktatDir: String =
                Paths.get("../diktat-ruleset/target")
                    .takeIf { it.exists() }
                    ?.listDirectoryEntries()
                    ?.single { it.name.contains("diktat") }
                    ?.pathString ?: ""

            val diktat = File("src/test/resources/test/smoke/diktat.jar")
            val diktatFrom = File(diktatDir)
            val save = File("src/test/resources/test/smoke/${getSaveForCurrentOs()}")
            val ktlint = File("src/test/resources/test/smoke/ktlint")

            ktlint.createNewFile()
            save.createNewFile()
            diktat.createNewFile()

            downloadFile("https://github.com/saveourtool/save-cli/releases/download/v$SAVE_VERSION/${getSaveForCurrentOs()}", save)
            downloadFile("https://github.com/pinterest/ktlint/releases/download/$KTLINT_VERSION/ktlint", ktlint)

            FileUtils.copyFile(diktatFrom, diktat)
        }

        @AfterAll
        @JvmStatic
        internal fun afterAll() {
            val diktat = File("src/test/resources/test/smoke/diktat.jar")
            val configFile = File("src/test/resources/test/smoke/diktat-analysis.yml")
            val save = File("src/test/resources/test/smoke/${getSaveForCurrentOs()}")
            val ktlint = File("src/test/resources/test/smoke/ktlint")

            diktat.delete()
            configFile.delete()
            ktlint.delete()
            save.delete()
        }
    }
}
