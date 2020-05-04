package test_framework.processing

import org.apache.commons.io.FileUtils
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList
import java.util.stream.Collectors

class TestComparatorUnit(private val resourceFilePath: String, private val function: (f: String) -> String) {

    fun compareFilesFromResources(expectedResult: String, testFileStr: String): Boolean {
        val expectedFile = File(javaClass.classLoader.getResource("$resourceFilePath/$expectedResult")!!.file)
        val testFile = File(javaClass.classLoader.getResource("$resourceFilePath/$testFileStr")!!.file)

        val copyTestFile = File("${testFile.absolutePath}_copy")
        FileUtils.copyFile(testFile, copyTestFile)

        val actualResult = function(readFile(copyTestFile.absolutePath).joinToString("\n"))

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
