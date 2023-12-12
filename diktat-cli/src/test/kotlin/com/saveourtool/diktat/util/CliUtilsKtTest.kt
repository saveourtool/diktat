package com.saveourtool.diktat.util

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
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

        Assertions.assertThat(tmpDir.listFiles("**/Test1.kt").toList())
            .containsExactlyInAnyOrder(
                tmpDir.resolve("folder1").resolve("subFolder11").resolve("Test1.kt"),
                tmpDir.resolve("folder1").resolve("subFolder12").resolve("Test1.kt"),
                tmpDir.resolve("folder2").resolve("Test1.kt"),
            )
    }


    @Test
    fun walkByGlobWithGlobalPath(@TempDir tmpDir: Path) {
        setupHierarchy(tmpDir)

        Assertions.assertThat(tmpDir.listFiles("${tmpDir.absolutePathString()}${File.separator}**${File.separator}Test2.kt").toList())
            .containsExactlyInAnyOrder(
                tmpDir.resolve("folder1").resolve("subFolder11").resolve("Test2.kt"),
                tmpDir.resolve("folder2").resolve("Test2.kt"),
            )
    }

    @Test
    fun walkByGlobWithRelativePath(@TempDir tmpDir: Path) {
        setupHierarchy(tmpDir)

        val expectedResult = arrayOf(
            tmpDir.resolve("folder1").resolve("subFolder11").resolve("Test1.kt"),
            tmpDir.resolve("folder1").resolve("subFolder11").resolve("Test2.kt"),
        )
        Assertions.assertThat(tmpDir.listFiles("folder1/subFolder11/*.kt").toList())
            .containsExactlyInAnyOrder(*expectedResult)
        Assertions.assertThat(tmpDir.listFiles("folder1\\subFolder11\\*.kt").toList())
            .containsExactlyInAnyOrder(*expectedResult)
    }

    @Test
    fun walkByGlobWithEmptyResult(@TempDir tmpDir: Path) {
        setupHierarchy(tmpDir)

        Assertions.assertThat(tmpDir.listFiles("**/*.kts").toList())
            .isEmpty()
    }

    @Test
    fun walkByGlobWithParentFolder(@TempDir tmpDir: Path) {
        setupHierarchy(tmpDir)

        Assertions.assertThat(tmpDir.resolve("folder1").listFiles("../*/*.kt").toList())
            .containsExactlyInAnyOrder(
                tmpDir.resolve("folder2").resolve("Test1.kt"),
                tmpDir.resolve("folder2").resolve("Test2.kt"),
                tmpDir.resolve("folder2").resolve("Test3.kt"),
            )
    }

    @Test
    fun walkByGlobWithFolder(@TempDir tmpDir: Path) {
        setupHierarchy(tmpDir)

        Assertions.assertThat(tmpDir.listFiles("folder2").toList())
            .containsExactlyInAnyOrder(
                tmpDir.resolve("folder2").resolve("Test1.kt"),
                tmpDir.resolve("folder2").resolve("Test2.kt"),
                tmpDir.resolve("folder2").resolve("Test3.kt"),
            )


        Assertions.assertThat(tmpDir.listFiles("folder1").toList())
            .containsExactlyInAnyOrder(
                tmpDir.resolve("folder1").resolve("subFolder11").resolve("Test1.kt"),
                tmpDir.resolve("folder1").resolve("subFolder11").resolve("Test2.kt"),
                tmpDir.resolve("folder1").resolve("subFolder12").resolve("Test1.kt"),
            )
    }

    companion object {
        private fun Path.resolveAndCreateDirectory(name: String): Path = resolve(name).also {
            it.createDirectory()
        }

        private fun Path.resolveAndCreateFile(name: String): Path = resolve(name).also {
            it.createFile().writeText("Test file: $name")
        }
    }
}
