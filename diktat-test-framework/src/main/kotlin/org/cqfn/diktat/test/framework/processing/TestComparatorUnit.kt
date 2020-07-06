package org.cqfn.diktat.test.framework.processing

import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList
import java.util.stream.Collectors

@Suppress("ForbiddenComment")
class TestComparatorUnit(private val resourceFilePath: String,
                         private val function: (expectedText: String, testFilePath: String) -> String) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(TestComparatorUnit::class.java)
    }

    fun compareFilesFromResources(expectedResult: String, testFileStr: String): Boolean {
        val expectedPath = javaClass.classLoader.getResource("$resourceFilePath/$expectedResult")
        val testPath = javaClass.classLoader.getResource("$resourceFilePath/$testFileStr")
        if (testPath == null || expectedPath == null) {
            log.error("Not able to find files for running test: $expectedResult and $testFileStr")
            return false
        }

        val expectedFile = File(expectedPath.file)
        val testFile = File(testPath.file)

        val copyTestFile = File("${testFile.absolutePath}_copy")
        FileUtils.copyFile(testFile, copyTestFile)

        val actualResult = function(
                readFile(copyTestFile.absolutePath).joinToString("\n"),
                copyTestFile.absolutePath
        )

        // fixme: actualResult is separated by KtLint#determineLneSeparator, should be split by it here too
        return FileComparator(expectedFile, actualResult.split("\n")).compareFilesEqual()
    }

    fun readFile(fileName: String): List<String> {
        var list: List<String> = ArrayList()
        try {
            Files.newBufferedReader(Paths.get(fileName)).use { list = it.lines().collect(Collectors.toList()) }
        } catch (e: IOException) {
            println("Not able to read file: $fileName")
        }
        return list
    }
}
