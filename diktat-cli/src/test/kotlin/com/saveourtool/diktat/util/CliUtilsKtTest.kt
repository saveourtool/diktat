package com.saveourtool.diktat.util

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledOnOs
import org.junit.jupiter.api.condition.OS
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.absolutePathString
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.writeText

class CliUtilsKtTest {
    @Test
    fun listByFilesWithLeadingAsterisks() {
        Assertions.assertThat(tmpDir.listFiles("**/Test1.kt").toList())
            .containsExactlyInAnyOrder(
                tmpDir.resolve("folder1").resolve("subFolder11").resolve("Test1.kt"),
                tmpDir.resolve("folder1").resolve("subFolder12").resolve("Test1.kt"),
                tmpDir.resolve("folder2").resolve("Test1.kt"),
            )
    }

    @Test
    fun listByFilesWithGlobalPath() {
        Assertions.assertThat(tmpDir.listFiles("${tmpDir.absolutePathString()}${File.separator}**${File.separator}Test2.kt").toList())
            .containsExactlyInAnyOrder(
                tmpDir.resolve("folder1").resolve("subFolder11").resolve("Test2.kt"),
                tmpDir.resolve("folder2").resolve("Test2.kt"),
            )
    }

    @Test
    fun listByFilesWithGlobalPattern() {
        Assertions.assertThat(tmpDir.resolve("folder2").listFiles("${tmpDir.absolutePathString()}${File.separator}**${File.separator}Test2.kt").toList())
            .containsExactlyInAnyOrder(
                tmpDir.resolve("folder1").resolve("subFolder11").resolve("Test2.kt"),
                tmpDir.resolve("folder2").resolve("Test2.kt"),
            )
    }

    @Test
    fun listByFilesWithRelativePath() {
        Assertions.assertThat(tmpDir.listFiles("folder1/subFolder11/*.kt").toList())
            .containsExactlyInAnyOrder(
                tmpDir.resolve("folder1").resolve("subFolder11").resolve("Test1.kt"),
                tmpDir.resolve("folder1").resolve("subFolder11").resolve("Test2.kt"),
            )
    }

    @Test
    @EnabledOnOs(OS.WINDOWS)
    fun listByFilesWithRelativePathWindows() {
        Assertions.assertThat(tmpDir.listFiles("folder1\\subFolder11\\*.kt").toList())
            .containsExactlyInAnyOrder(
                tmpDir.resolve("folder1").resolve("subFolder11").resolve("Test1.kt"),
                tmpDir.resolve("folder1").resolve("subFolder11").resolve("Test2.kt"),
            )
    }

    @Test
    fun listByFilesWithEmptyResult() {
        Assertions.assertThat(tmpDir.listFiles("**/*.kts").toList())
            .isEmpty()
    }

    @Test
    fun listByFilesWithParentFolder() {
        Assertions.assertThat(tmpDir.resolve("folder1").listFiles("../*/*.kt").toList())
            .containsExactlyInAnyOrder(
                tmpDir.resolve("folder2").resolve("Test1.kt"),
                tmpDir.resolve("folder2").resolve("Test2.kt"),
                tmpDir.resolve("folder2").resolve("Test3.kt"),
            )
    }

    @Test
    fun listByFilesWithFolder() {
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

    @Test
    fun listByFilesWithNegative() {
        Assertions.assertThat(tmpDir.listFiles("**/*.kt", "!**/subFolder11/*.kt", "!**/Test3.kt").toList())
            .containsExactlyInAnyOrder(
                tmpDir.resolve("folder1").resolve("subFolder12").resolve("Test1.kt"),
                tmpDir.resolve("folder2").resolve("Test1.kt"),
                tmpDir.resolve("folder2").resolve("Test2.kt"),
            )
    }

    companion object {
        @JvmStatic
        @TempDir
        internal var tmpDir: Path = Paths.get("/invalid")

        @BeforeAll
        @JvmStatic
        internal fun setupHierarchy() {
            tmpDir.resolveAndCreateDirectory("folder1")
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
            tmpDir.resolveAndCreateDirectory("folder2")
                .also { folder2 ->
                    folder2.resolveAndCreateFile("Test1.kt")
                    folder2.resolveAndCreateFile("Test2.kt")
                    folder2.resolveAndCreateFile("Test3.kt")
                }
        }

        private fun Path.resolveAndCreateDirectory(name: String): Path = resolve(name).also {
            it.createDirectory()
        }

        private fun Path.resolveAndCreateFile(name: String): Path = resolve(name).also {
            it.createFile().writeText("Test file: $name")
        }
    }
}
