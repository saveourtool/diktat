package org.cqfn.diktat.util

import mu.KotlinLogging
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.FileSystems
import java.nio.file.Path
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.writeText

class CliUtilsKtTest {
    private fun setupHierarchy(dir: Path) {
        dir.resolveAndCreateDirectory("folder1")
            .also { folder1 ->
                folder1.resolveAndCreateDirectory("subFolder11")
                    .also { subFolder11 ->
                        subFolder11.resolveAndCreateFile("Test1.kt")
                        subFolder11.resolveAndCreateFile("Test2.kt")
                    }
                folder1.resolveAndCreateDirectory("subFolder12")
                    .also { subFolder12 ->
                        subFolder12.resolveAndCreateFile("Test1.kt")
                    }
            }
        dir.resolveAndCreateDirectory("folder2")
            .also { folder2 ->
                folder2.resolveAndCreateFile("Test1.kt")
                folder2.resolveAndCreateFile("Test2.kt")
                folder2.resolveAndCreateFile("Test3.kt")
            }
    }

    @Test
    fun walkByGlobWithLeadingAsterisks(@TempDir tmpDir: Path) {
        setupHierarchy(tmpDir)
        Assertions.assertEquals(
            listOf(
                tmpDir.resolve("folder1").resolve("subFolder11").resolve("Test1.kt"),
                tmpDir.resolve("folder1").resolve("subFolder12").resolve("Test1.kt"),
                tmpDir.resolve("folder2").resolve("Test1.kt"),
            ),
            tmpDir.walkByGlob("**/Test1.kt").toList()
        )
    }


    @Test
    fun walkByGlobWithGlobalPath(@TempDir tmpDir: Path) {
        setupHierarchy(tmpDir)

        Assertions.assertEquals(
            listOf(
                tmpDir.resolve("folder1").resolve("subFolder11").resolve("Test2.kt"),
                tmpDir.resolve("folder2").resolve("Test2.kt"),
            ),
            tmpDir.walkByGlob("${tmpDir.absolutePathString()}${File.separator}**${File.separator}Test2.kt").toList()
        )
    }

    @Test
    fun walkByGlobWithRelativePath(@TempDir tmpDir: Path) {
        setupHierarchy(tmpDir)
        Assertions.assertEquals(
            listOf(
                tmpDir.resolve("folder1").resolve("subFolder11").resolve("Test1.kt"),
                tmpDir.resolve("folder1").resolve("subFolder11").resolve("Test2.kt"),
            ),
            tmpDir.walkByGlob("folder1/subFolder11/*.kt").toList()
        )
    }

    @Test
    fun walkByGlobWithEmptyResult(@TempDir tmpDir: Path) {
        setupHierarchy(tmpDir)
        Assertions.assertEquals(
            emptyList<Path>(),
            tmpDir.walkByGlob("**/*.kts").toList()
        )
    }

    @Test
    fun notEmptyRoots() {
        val fromFile = File.listRoots()
            .also {
                log.info { "listRoots is empty: ${it.iterator().hasNext()}" }
            }
            .iterator()
            .asSequence()
            .map {
                it.toPath().absolutePathString()
            }
            .onEach {
                log.info { "listRoots: $it" }
            }
            .toList()
        assert(fromFile.isEmpty()) {
            "File.listRoots is not empty: ${fromFile.joinToString()}"
        }
        val fromFileSystem = FileSystems.getDefault().rootDirectories
            .also {
                log.info { "rootDirectory is empty: ${it.iterator().hasNext()}" }
            }
            .asSequence()
            .map { it.absolutePathString() }
            .onEach {
                log.info { "rootDirectory: $it" }
            }
            .toList()

        assert(fromFileSystem.isEmpty()) {
            "FileSystems.getDefault().rootDirectories is not empty: ${fromFileSystem.joinToString()}"
        }
    }

    companion object {
        private val log = KotlinLogging.logger {}

        private fun Path.resolveAndCreateDirectory(name: String): Path = resolve(name).also {
            it.createDirectory()
        }

        private fun Path.resolveAndCreateFile(name: String): Path = resolve(name).also {
            it.createFile().writeText("Test file: $name")
        }
    }
}
